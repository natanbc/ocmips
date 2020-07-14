#pragma once

#define NULL ((void*)0)

#define offsetof(st, m) \
    __builtin_offsetof(st, m)

#define container_of(ptr, type, member) ({                \
    const typeof( ((type *)0)->member ) *__mptr = (ptr);  \
    (type *)( (char *)__mptr - offsetof(type,member) );})

#define align_up(num, align) (((num) + ((align) -1)) & ~((align) -1))

typedef signed int   intptr_t;
typedef unsigned int uintptr_t;

typedef signed int   ssize_t;
typedef unsigned int size_t;

typedef signed   int int32_t;
typedef unsigned int uint32_t;

typedef signed   short int16_t;
typedef unsigned short uint16_t;

typedef signed   char int8_t;
typedef unsigned char uint8_t;

