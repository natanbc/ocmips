void* memset(void* dst, int c, unsigned int length) {
    char* s = (char*)dst;
    while(length-- != 0) {
        *s++ = (char) c;
    }
    return dst;
}

