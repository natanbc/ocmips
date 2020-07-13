TYPE ?= eeprom

AR=llvm-ar-10
AS=clang-10
CC=clang-10
LD=ld.lld-10
OBJCOPY=llvm-objcopy-10

ifeq (${TYPE},eeprom)
    BASE=0x80000000
    LD_SCRIPT=eeprom.ld
    BASE_STACK=0x50000000+0xf000
else ifeq (${TYPE},bootrom)
    BASE=0x40000000
    LD_SCRIPT=bootrom.ld
    BASE_STACK=0x50000000+0xf000
else
    $(error Unknown ROM type ${TYPE})
endif

override BASE_FLAGS += \
    -target mips-none -march=mips2 -mno-madd4 -mno-msa -mno-mt      \
    -mno-virt -mno-xgot -mno-dsp -mno-dspr2 -mabicalls -msoft-float \
    -mllvm -disable-mips-delay-filler=true -mno-gpopt -mno-crc      \
    -nostdlib
override CFLAGS += \
    ${BASE_FLAGS} -fdata-sections -ffunction-sections -Ilibrt
override ASMFLAGS += \
    ${BASE_FLAGS} -DRT_SP=${BASE_STACK} -DRT_FP=0 -DRT_GP=0
override LDFLAGS += \
    --image-base=${BASE} --section-start=.text=${BASE} --gc-sections  \
    --script ${LD_SCRIPT} --defsym=BASE_ADDR=${BASE}
override OBJFLAGS += \
    --set-start=${BASE} -S --strip-unneeded -O binary

.PHONY: elf binary clean

LIBS=librt/librt.a
OBJS=build.o

build.o:
ifndef PROGRAM
	$(error PROGRAM is not defined)
endif
	$(CC) $(CFLAGS) src/$(PROGRAM).c -c -o $@

build.elf: librt_target $(OBJS)
	$(LD) $(LDFLAGS) -o $@ $(LIBS) $(OBJS)
build.bin: build.elf
	$(OBJCOPY) $(OBJFLAGS) build.elf $@

elf: build.elf
binary: build.bin

clean:
	rm -rf *.o *.elf *.bin
	make -C $(LIBRT_DIR) clean

LIBRT_DIR=librt
LIBRT_TGT=librt.a
librt_target:
	make -C $(LIBRT_DIR) AR="$(AR)" AS="$(AS)" CC="$(CC)" CFLAGS="$(CFLAGS)" ASMFLAGS="$(ASMFLAGS)" $(LIBRT_TGT)
