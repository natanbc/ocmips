#include "crt.h"

void* memcpy(void* dest, const void* src, unsigned int n) {
    char* d = dest;
    const char* s = src;
    while(n != 0) {
        *d = *s;
        d++;
        s++;
        n--;
    }
    return dest;
}


void* memset(void* dst, int c, unsigned int length) {
    char* s = (char*)dst;
    while(length-- != 0) {
        *s++ = (char) c;
    }
    return dst;
}

int strncmp(const char* a, const char* b, unsigned int n) {
    while(n != 0 && *a != 0 && (*a == *b)) {
        a++;
        b++;
        n--;
    }
    if(n == 0) return 0;
    return (*(unsigned char*)a - *(unsigned char*)b);
}

