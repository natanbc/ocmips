#include "rt.h"

int rt_map_framebuffer(volatile framebuffer_t* addr, address_t* gpu) {
    return rt_map_special_component(addr, gpu, SPECIAL_COMPONENT_GPU, 0);
}

int rt_map_drive(volatile drive_t* addr, address_t* drive, int mode) {
    return rt_map_special_component(addr, drive, SPECIAL_COMPONENT_DRIVE, mode);
}

int rt_map_eeprom(volatile eeprom_t* addr, address_t* eeprom) {
    return rt_map_special_component(addr, eeprom, SPECIAL_COMPONENT_EEPROM, 0);
}

volatile int* rt_get_eeprom_content(volatile eeprom_t* eeprom) {
    return eeprom->__internal;
}

volatile int* rt_get_eeprom_data(volatile eeprom_t* eeprom) {
    return &eeprom->__internal[(eeprom->size / 4) - 1];
}

void rt_sync_framebuffer(volatile framebuffer_t* fb) {
    fb->sync = 1;
}

void rt_clear_framebuffer(volatile framebuffer_t* fb) {
    fb->sync = 2;
}

void rt_sync_drive(volatile drive_t* drive, int mode) {
    drive->sync = mode;
}

void rt_sync_eeprom(volatile eeprom_t* eeprom, int mode) {
    eeprom->sync = mode;
}

void rt_call_method(volatile component_method_t* method) {
    method->call = 1;
}
