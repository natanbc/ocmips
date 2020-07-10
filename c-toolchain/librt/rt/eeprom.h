#pragma once

#include "rt/common.h"

#define EEPROM_SYNC_DISCARD      0
#define EEPROM_SYNC_WRITE        1

typedef struct {
    int sync;
    int dirty;
    int size;
    int data_size;
    int __internal[];
} eeprom_t;

void rt_sync_eeprom(volatile eeprom_t* eeprom, int mode);

int rt_map_eeprom(volatile eeprom_t* addr, address_t* eeprom);

volatile int* rt_get_eeprom_content(volatile eeprom_t* eeprom);

volatile int* rt_get_eeprom_data(volatile eeprom_t* eeprom);
