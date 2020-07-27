AR ?= ar
AS ?= as
CC ?= cc
CFLAGS ?= -Wall -Wextra -Werror
ASMFLAGS ?= -Wall -Wextra -Werror

OBJS=rt_init.o rt_syscalls.o rt_helpers.o rt_helpers_asm.o crt.o malloc.o

librt.a: $(OBJS)
	$(AR) rcs $@ $(OBJS)

# always build librt optimized for size
.c.o:
	$(CC) $(CFLAGS) -Os -I. -c -o $@ $<

.S.o:
	$(AS) $(ASMFLAGS) -c -o $@ $<

clean:
	rm -rf *.o *.a
