#include "rt.h"

#define EEPROM_POS (0x80000000 - offsetof(eeprom_t, __internal))
MEMORY_MAP(eeprom_t,           eeprom, EEPROM_POS);
MEMORY_MAP(component_method_t, method, 0x88000000); //only one may be active at a time
MEMORY_MAP(framebuffer_t,      fb,     0x88000000); //only one may be active at a time

static inline int float2integer(float f) {
    union { int i; float f; } u;
    u.f = f;
    return u.i;
}

void invoke1(address_t* addr, const char* name, int type, int value) {
    rt_map_component(method, addr, name, 1);
    method->argc = 1;
    method->args[0].type = type;
    method->args[0].value = value;
    rt_call_method(method);
    rt_unmap(method);
}

void beep() {
    address_t addr;
    rt_find_component("computer", 0, &addr);
    rt_map_component(method, &addr, "beep", 2);
    method->argc = 2;
    method->args[0].type = TYPE_SHORT;
    method->args[0].value = 400;
    method->args[1].type = TYPE_FLOAT;
    method->args[1].value = float2integer(0.02);
    rt_call_method(method);
    rt_unmap(method);
}

void get_gpu_res(address_t* addr, int* w, int* h) {
    rt_map_framebuffer(fb, addr);
    *w = fb->width;
    *h = fb->height;
    rt_unmap(fb);
}

void clear_gpu(address_t* gpu, int w, int h) {
    rt_map_component(method, gpu, "fill", 5);
    method->argc = 5;
    method->args[0].type = TYPE_INTEGER;
    method->args[0].value = 1;
    method->args[1].type = TYPE_INTEGER;
    method->args[1].value = 1;
    method->args[2].type = TYPE_INTEGER;
    method->args[2].value = w;
    method->args[3].type = TYPE_INTEGER;
    method->args[3].value = h;
    method->args[4].type = TYPE_STRING;
    method->args[4].value = (int)" ";
    rt_call_method(method);
    rt_unmap(method);
}

int main() {
    beep();
    address_t screen;
    address_t gpu;
    int has_gpu = rt_find_component("gpu", 0, &gpu) == 0;
    int has_screen = rt_find_component("screen", 0, &screen) == 0;
    int can_render = has_gpu & has_screen;
    if(has_gpu && has_screen) {
        invoke1(&gpu, "bind", TYPE_ADDRESS, (int)&screen);
    }
    if(has_gpu) {
        invoke1(&gpu, "setForeground", TYPE_INTEGER, 0xFFFFFF);
        invoke1(&gpu, "setBackground", TYPE_INTEGER, 0x000000);
        int w, h;
        get_gpu_res(&gpu, &w, &h);
        clear_gpu(&gpu, w, h);
    }
    address_t addr;
    if(rt_find_component("eeprom", 0, &addr) != 0) {
        rt_bsod(can_render ? &gpu : 0, "ERR_NO_EEPROM");
    }
    if(rt_map_eeprom(eeprom, &addr) != 0) {
        rt_bsod(can_render ? &gpu : 0, "ERR_EEPROM_MAP_FAIL");
    }
    ((void(*)())rt_get_eeprom_content(eeprom))();
    rt_bsod(can_render ? &gpu : 0, "ERR_EEPROM_RETURNED");
}
