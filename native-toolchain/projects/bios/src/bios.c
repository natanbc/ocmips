#include "rt.h"

//void ibin_main(int heap_start, int heap_size);
typedef void (*ibin_main_t)(int, int);

typedef struct __attribute__((packed)) {
    char magic[4];   //must be "ibin"
    int base_addr;   //0 for position independent code
    int code_len;    //length of the code
    int main_offset; //offset of the main function
} ibin_header_t;

//no need to make it packed since this isn't part of our abi
typedef struct {
    char magic[4];     //must be "boot"
    address_t fs_addr; //address of the filesystem to boot
} boot_info_t;

#define M(name,addr) MEMORY_MAP(component_method_t, name, addr)

M(m_close,  0x1000000);
M(m_exists, 0x1000100);
M(m_open,   0x1000200);
M(m_read,   0x1000300);
//M(m_size,   0x1000400);
MEMORY_MAP(eeprom_t, self, 0x1000500);

address_t gpu;
int has_gpu;

#define BIOS_CHECK(r) \
    do { if((r) != 0) die("assert fail", __func__, __LINE__); } while(0)
#define DIE(msg) die(msg, __func__, __LINE__)

static void die(const char* msg, const char* func, int line) {
    rt_dbg("Crash", TYPE_STRING, (int)msg);
    rt_dbg("Crashed at", TYPE_STRING, (int)func);
    rt_dbg("Line", TYPE_INTEGER, line);
    rt_bsod(has_gpu?&gpu : 0, msg);
}

static int try_map_fs(address_t* fs) {
    int r;
    if((r = rt_map_component(m_close,  fs, "close",  1)) != 0) return r;
    if((r = rt_map_component(m_exists, fs, "exists", 1)) != 0) return r;
    if((r = rt_map_component(m_open,   fs, "open",   1)) != 0) return r;
    if((r = rt_map_component(m_read,   fs, "read",   2)) != 0) return r;
    m_close->argc = 1;
    m_close->args[0].type = TYPE_INTEGER;
    m_exists->argc = 1;
    m_exists->args[0].type = TYPE_STRING;
    m_open->argc = 1;
    m_open->args[0].type = TYPE_STRING;
    m_read->argc = 2;
    m_read->args[0].type = TYPE_INTEGER;
    m_read->args[1].type = TYPE_INTEGER;
    return 0;
}

static void unmap_fs() {
    rt_unmap(m_exists);
    rt_unmap(m_close);
    rt_unmap(m_open);
    rt_unmap(m_read);
}

static void fs_close(int fd) {
    m_close->args[0].value = fd;
    rt_call_method(m_close);
}

static int fs_invoke1i(volatile component_method_t* m, const char* path) {
    component_method_ret_t buf[2];
    m->ret_buf = buf;
    m->ret_buf_size = sizeof(buf);
    m->args[0].value = (int)path;
    rt_call_method(m);
    return buf[0].value;
}

static int fs_exists(const char* path) {
    return fs_invoke1i(m_exists, path) != 0;
}

static int fs_open(const char* path) {
    return fs_invoke1i(m_open, path);
}

//buffer must be at least `RET_SIZE_BYTEARRAY(bytes)` bytes long
static int fs_read(int fd, uint8_t* buffer, int bytes) {
    m_read->ret_buf = (component_method_ret_t*)buffer;
    m_read->ret_buf_size = RET_SIZE_BYTEARRAY(bytes);
    m_read->args[0].value = fd;
    m_read->args[1].value = bytes;
    rt_call_method(m_read);
    return ((component_method_ret_t*)buffer)->type != TYPE_BYTE_ARRAY;
}

static void find_gpu() {
    has_gpu = rt_find_component("gpu", 0, &gpu) == 0;
}

static int read_ibin_header(int fd, ibin_header_t* h) {
    uint8_t read_retbuf[RET_SIZE_BYTEARRAY(sizeof(ibin_header_t))];
    BIOS_CHECK(fs_read(fd, read_retbuf, sizeof(ibin_header_t)));
    byte_array_t* arr = (byte_array_t*)((component_method_ret_t*)read_retbuf)->value;
    if(arr->len < sizeof(ibin_header_t)) return 1;
    ibin_header_t* header = (ibin_header_t*)arr->data;
    if(strncmp(header->magic, "ibin", 4) != 0) return 1;
    memcpy(h, header, sizeof(ibin_header_t));
    return 0;
}

static void find_boot() {
    address_t eeprom;
    address_t fs;
    BIOS_CHECK(rt_find_component("eeprom", 0, &eeprom));
    BIOS_CHECK(rt_map_eeprom(self, &eeprom));
    boot_info_t* b = (boot_info_t*)rt_get_eeprom_data(self);
    int needs_recheck = 0;
    if(strncmp(b->magic, "boot", 4) != 0 || try_map_fs(&b->fs_addr) != 0) {
        needs_recheck = 1;
    } else {
        if(!fs_exists("/init.bin")) {
            needs_recheck = 1;
        } else {
            int fd = fs_open("/init.bin");
            ibin_header_t header;
            if(read_ibin_header(fd, &header) != 0) needs_recheck = 1;
            fs_close(fd);
        }
    }
    if(needs_recheck) {
        unmap_fs();
        int ret;
        int i = 0;
        while((ret = rt_find_component("filesystem", i++, &fs)) == 0) {
            BIOS_CHECK(try_map_fs(&fs));
            if(fs_exists("/init.bin")) {
                int fd = fs_open("/init.bin");
                ibin_header_t header;
                int read_result = read_ibin_header(fd, &header);
                fs_close(fd);
                if(read_result == 0) {
                    memcpy(&b->magic[0], "boot", 4);
                    memcpy(&b->fs_addr, fs, sizeof(address_t));
                    rt_sync_eeprom(self, EEPROM_SYNC_WRITE);
                    break;
                }
            }
            unmap_fs();
        }
        if(ret != 0) {
            DIE("Unable to find boot filesystem");
        }
    }
    rt_unmap(self);
}

static void load() {
    int fd = fs_open("/init.bin");
    ibin_header_t header;
    BIOS_CHECK(read_ibin_header(fd, &header));
    uint8_t buffer[RET_SIZE_BYTEARRAY(256)];
    uint8_t* dest = (uint8_t*)0x50000000;
    while(fs_read(fd, buffer, 256) == 0) {
        byte_array_t* arr = (byte_array_t*)((component_method_ret_t*)buffer)->value;
        //for(int i = 0; i < arr->len; i++) dest[i] = arr->data[i];
        memcpy(dest, arr->data, arr->len);
        dest += arr->len;
    }
    fs_close(fd);
    unmap_fs();
    ((ibin_main_t)(0x50000000+header.main_offset))(
        (int)dest,
        (rt_ramsize() - RT_STACK_SIZE) - ((int)dest - 0x50000000)
    );
    DIE("ibin_main() returned");
}

int main() {
    find_gpu();
    find_boot();
    load();
}

