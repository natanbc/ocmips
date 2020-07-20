//reimplementation of https://git.asie.pl/asie-minecraft/Nadeshicodec/src/branch/master/lua/rin.lua

#include "rt.h"
#include "nadeshicodec_gen.h"

#define CONSUME_BUDGET(amt) ((call_budget_tick)+=(amt))

int call_budget_tick = 0;
int curr_bg = 0;
int curr_fg = 0;
int frame = 0;

float frame_offset = 0;

MEMORY_MAP(component_method_t, set_bg_method, 0x60000000);
MEMORY_MAP(component_method_t, set_fg_method, 0x60000100);
MEMORY_MAP(component_method_t, set_method,    0x60000200);
MEMORY_MAP(component_method_t, fill_method,   0x60000300);
MEMORY_MAP(component_method_t, set_palette,   0x60000400);
MEMORY_MAP(component_method_t, set_res,       0x60000500);
MEMORY_MAP(component_method_t, alloc_buf,     0x60000600);
MEMORY_MAP(component_method_t, set_buf,       0x60000700);
MEMORY_MAP(component_method_t, bitblt,        0x60000800);
MEMORY_MAP(drive_t,            drv,           0x60001000);

static void init_methods(address_t* gpu) {
#define TRY(c) do { if((c)!=0) goto fail; } while(0)
    //setBackground(int)
    TRY(rt_map_component(set_bg_method, gpu, "setBackground", 1));
    set_bg_method->argc = 1;
    set_bg_method->args[0].type = TYPE_INTEGER;
    //setForeground(int)
    TRY(rt_map_component(set_fg_method, gpu, "setForeground", 1));
    set_fg_method->argc = 1;
    set_fg_method->args[0].type = TYPE_INTEGER;
    //set(int, int, String, boolean)
    TRY(rt_map_component(set_method, gpu, "set", 4));
    set_method->argc = 4;
    for(int i = 0; i < 2; i++) {
        set_method->args[i].type = TYPE_INTEGER;
    }
    set_method->args[2].type = TYPE_STRING;
    set_method->args[3].type = TYPE_BOOLEAN;
    //fill(int, int, int, int, String)
    TRY(rt_map_component(fill_method, gpu, "fill", 5));
    fill_method->argc = 5;
    for(int i = 0; i < 4; i++) {
        fill_method->args[i].type = TYPE_INTEGER;
    }
    fill_method->args[4].type = TYPE_STRING;
    //setPaletteColor(int, int)
    TRY(rt_map_component(set_palette, gpu, "setPaletteColor", 2));
    set_palette->argc = 2;
    for(int i = 0; i < 2; i++) {
        set_palette->args[i].type = TYPE_INTEGER;
    }
    //setResolution(int, int)
    TRY(rt_map_component(set_res, gpu, "setResolution", 2));
    set_res->argc = 2;
    for(int i = 0; i < 2; i++) {
        set_res->args[i].type = TYPE_INTEGER;
    }
    //allocateBuffer(): number
    TRY(rt_map_component(alloc_buf, gpu, "allocateBuffer", 0));
    alloc_buf->argc = 0;
    //setActiveBuffer(int)
    TRY(rt_map_component(set_buf, gpu, "setActiveBuffer", 1));
    set_buf->argc = 1;
    set_buf->args[0].type = TYPE_INTEGER;
    //bitblt()
    TRY(rt_map_component(bitblt, gpu, "bitblt", 0));
    bitblt->argc = 0;
    return;
#undef TRY
fail:
    rt_bsod(gpu, "method mapping failed");
}

static void init_buf() {
    component_method_ret_t r[1];
    alloc_buf->ret_buf = r;
    alloc_buf->ret_buf_size = sizeof(r);
    rt_call_method(alloc_buf);
    rt_dbg("Allocated buffer", TYPE_INTEGER, r[0].value);
    set_buf->args[0].value = r[0].value;
    rt_call_method(set_buf);
}

static void sync_screen() {
    rt_call_method(bitblt);
}

static void init_palette() {
    for(int i = 0; i < 16; i++) {
        set_palette->args[0].value = i;
        set_palette->args[1].value = PALETTE[i];
        rt_call_method(set_palette);
    }
}

static void set_bg(int b) {
    if(b != curr_bg) {
        CONSUME_BUDGET(2);
        set_bg_method->args[0].value = PALETTE[b];
        rt_call_method(set_bg_method);
        curr_bg = b;
    }
}

static void set_fg(int f) {
    if(f != curr_fg) {
        CONSUME_BUDGET(2);
        set_fg_method->args[0].value = PALETTE[f];
        rt_call_method(set_fg_method);
        curr_fg = f;
    }
}

static void g_set(int x, int y, const uint8_t* s, int vertical) {
    CONSUME_BUDGET(1);
    set_method->args[0].value = x + 1;
    set_method->args[1].value = y + 1;
    set_method->args[2].value = (int)s;
    set_method->args[3].value = vertical;
    rt_call_method(set_method);
}

static void cmd_fill(int x, int y, int w, int h, int c) {
    int chr = 0;
    if(curr_fg == c) {
        chr = 1;
    } else if(curr_bg == c) {
        chr = 0;
    } else {
        set_bg(c);
    }
    if(w >= 2 && h >= 2) {
        CONSUME_BUDGET(2);
        fill_method->args[0].value = x + 1;
        fill_method->args[1].value = y + 1;
        fill_method->args[2].value = w;
        fill_method->args[3].value = h;
        fill_method->args[4].value = (int)QUAD(chr*255);
        rt_call_method(fill_method);
    } else { 
        const uint8_t* str;
        int vertical;
        if(w > 1) {
            str = STR(w-1, chr);
            vertical = 0;
        } else {
            str = STR(h-1, chr);
            vertical = 1;
        }
        g_set(x, y, str, vertical);
    }
}

uint8_t QSO[QUAD_LEN_PER_CHAR * 160 + 1];
uint8_t QS[160];

static void cmd_set(int x, int y, int bg, int fg, int vertical, uint8_t* qs, int qs_size) {
    int invert = 0;
    if(bg == curr_bg && fg == curr_fg) {
        // ok
    } else if(bg == curr_fg && fg == curr_bg) {
        invert = 255;
    } else if(bg == curr_bg) {
        set_fg(fg);
    } else if(fg == curr_bg) {
        set_fg(bg);
        invert = 255;
    } else if(fg == curr_fg) {
        set_bg(bg);
    } else if(bg == curr_fg) {
        set_bg(fg);
        invert = 255;
    } else {
        set_bg(bg);
        set_fg(fg);
    }
    uint8_t* qso = QSO;
    for(int i = 0; i < qs_size; i++) {
        int idx = qs[i] ^ invert;
        if(idx == 0) {
            *qso = *(QUAD(0)); //memcpy(qso, QUAD(0), 1);
            qso++;
        } else {
            memcpy(qso, QUAD(idx), QUAD_LEN_PER_CHAR);
            qso += QUAD_LEN_PER_CHAR;
        }
    }
    *qso = 0;
    g_set(x, y, QSO, vertical);
}

const uint8_t* drv_data;

static inline uint8_t r8() {
    uint8_t v = *drv_data;
    drv_data++;
    return v;
}

int main() {
    address_t gpu;
    RT_ASSERT_OK(rt_find_component("gpu", 0, &gpu));
    address_t drive;
    RT_ASSERT_OK(rt_find_component("drive", 0, &drive));
    RT_ASSERT_OK(rt_map_drive(drv, &drive, DRIVE_MAP_READONLY));
    drv_data = (const uint8_t*)(&drv->data);
    init_methods(&gpu);
    init_buf();
    init_palette();

    r8();
    set_res->args[0].value = r8();
    set_res->args[1].value = r8();
    rt_call_method(set_res);
    rt_dbg("Res W", TYPE_INTEGER, set_res->args[0].value);
    rt_dbg("Res H", TYPE_INTEGER, set_res->args[1].value);

    frame_offset = rt_uptime();

    while(1) {
        if(drv_data >= ((uint8_t*)&drv->data) + drv->size) {
            break;
        }
        int cmd = r8();
        switch(cmd) {
            case 0x10:
                cmd_fill(r8(), r8(), r8(), r8(), r8());
                break;
            case 0x18:
                cmd_fill(r8(), r8(), r8(), 1, r8());
                break;
            case 0x19:
                cmd_fill(r8(), r8(), 1, r8(), r8());
                break;
            case 0x12:
            case 0x13:
                {
                    int x = r8();
                    int y = r8();
                    int w = r8();
                    int bg = r8();
                    int fg = r8();
                    for(int i = 0; i < w; i++) {
                        QS[i] = r8();
                    }
                    cmd_set(x, y, bg, fg, cmd == 0x13, QS, w);
                }
                break;
            case 0x22:
            case 0x23:
                {
                    int x = r8();
                    int y = r8();
                    int bg = r8();
                    int fg = r8();
                    int len = 0;
                    while(1) {
                        uint8_t c = r8();
                        if(c == 0) break;
                        if(c >= 0xA1) {
                            int v = r8();
                            for(int i = 0; i < c - 0xA0; i++) {
                                QS[i] = v;
                            }
                            len += c - 0xA0;
                        } else {
                            for(int i = 0; i < c; i++) {
                                QS[i] = r8();
                            }
                            len += c;
                        }
                    }
                    cmd_set(x, y, bg, fg, cmd == 0x23, QS, len);
                }
                break;
            case 0x01:
                sync_screen();
                if(frame == 1) {
                    frame_offset = rt_uptime() + 0.05;
                } else {
                    frame_offset += 0.05;
                }
                frame++;
                while(rt_uptime() <= frame_offset) {
                    rt_sleep(1);
                }
                break;
            default:
                rt_dbg("Opcode", TYPE_INTEGER, cmd);
                rt_bsod(&gpu, "Unknown opcode");
        }
    }
    rt_dbg("Done!", 0, 0);
    while(1) rt_sleep(10);
}
