#pragma once

#include "rt/common.h"

#define TYPE_INTEGER            1
#define TYPE_FLOAT              2
#define TYPE_STRING             3
#define TYPE_ADDRESS            4
#define TYPE_NULL               5
#define TYPE_SHORT              6
#define TYPE_BOOLEAN            7
#define TYPE_BYTE_ARRAY         8

#define RET_SIZE_INTEGER        (sizeof(component_method_ret_t))
#define RET_SIZE_FLOAT          RET_SIZE_INTEGER
#define RET_SIZE_STRING(maxlen) (sizeof(component_method_ret_t)+align_up((maxlen)+1, 4))
#define RET_SIZE_ADDRESS        (sizeof(component_method_ret_t)+sizeof(address_t))
#define RET_SIZE_NULL           RET_SIZE_INTEGER
#define RET_SIZE_SHORT          RET_SIZE_INTEGER
#define RET_SIZE_BOOLEAN        RET_SIZE_INTEGER
#define RET_SIZE_BYTEARRAY(len) (sizeof(component_method_ret_t)+sizeof(int)+align_up((len), 4))

typedef struct {
    unsigned int len;
    uint8_t data[];
} byte_array_t;

typedef struct {
    int type;
    int value;
} component_method_arg_t;

typedef struct ___ret {
    int type;
    int value;
    struct ___ret* next;
} component_method_ret_t;

typedef struct {
    int call;
    component_method_ret_t* ret_buf;
    int ret_buf_size;
    int ret_count;
    int argc;
    component_method_arg_t args[];
} component_method_t;


void rt_call_method(volatile component_method_t* method);

int rt_map_component(volatile component_method_t* addr, address_t* component, const char* method, int max_args);

int rt_pull_signal(void* ret_buf, int ret_buf_size);

