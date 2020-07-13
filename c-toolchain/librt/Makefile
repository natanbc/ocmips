AR ?= ar
AS ?= as
CC ?= cc
CFLAGS ?= -Wall -Wextra -Werror
ASMFLAGS ?= -Wall -Wextra -Werror

OBJS=rt_init.o rt_syscalls.o rt_helpers.o crt.o

librt.a: $(OBJS)
	$(AR) rcs $@ $(OBJS)

.c.o:
	$(CC) $(CFLAGS) -I. -c -o $@ $<

.S.o:
	$(AS) $(ASMFLAGS) -c -o $@ $<

clean:
	rm -rf *.o *.a
