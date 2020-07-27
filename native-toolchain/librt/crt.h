#pragma once

#include "stddef.h"

#ifdef __cplusplus
extern "C" {
#endif

void* memcpy(void* dest, const void* src, size_t n);

void* memset(void* dst, int c, size_t length);

int strncmp(const char* a, const char* b, size_t n);

#ifdef __cplusplus
}
#endif
