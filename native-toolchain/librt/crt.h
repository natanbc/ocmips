#pragma once

#ifdef __cplusplus
extern "C" {
#endif

void* memcpy(void* dest, const void* src, unsigned int n);

void* memset(void* dst, int c, unsigned int length);

int strncmp(const char* a, const char* b, unsigned int n);

#ifdef __cplusplus
}
#endif
