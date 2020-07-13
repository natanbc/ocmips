#pragma once

#include "rt/common.h"

#define SPECIAL_COMPONENT_GPU    0
#define SPECIAL_COMPONENT_DRIVE  2
#define SPECIAL_COMPONENT_EEPROM 1

int rt_find_component(const char* type, int which, address_t* dest);

int rt_map_special_component(volatile void* addr, address_t* component, int type, int flags);

