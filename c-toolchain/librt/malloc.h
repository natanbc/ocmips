#pragma once

#include "stddef.h"

//allows for smaller binaries for programs that don't use malloc

//could maybe be moved to a compile-time define?
//make WITH_MALLOC=1 binary?
void rt_malloc_init(void);

void* malloc(size_t size);
void free(void* ptr);


