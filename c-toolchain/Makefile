TYPE ?= eeprom

AR=llvm-ar-10
AS=clang-10
CC=clang-10
LD=ld.lld-10
OBJCOPY=llvm-objcopy-10

RAM_START=0x50000000

RESULT=binary
STACK_SIZE=0x8000
STACK_ADDR=0x58000000
ENABLE_MALLOC=false
ifeq (${TYPE},eeprom)
    BASE=0x80000000
    LD_SCRIPT=eeprom.ld
else ifeq (${TYPE},bootrom)
    BASE=0x40000000
    LD_SCRIPT=bootrom.ld
else ifeq (${TYPE},initbin)
    BASE=0x50000000
    LD_SCRIPT=initbin.ld
    RESULT=init.bin
    ENABLE_MALLOC=true
else
    $(error Unknown ROM type ${TYPE})
endif

all: ${RESULT}


override BASE_FLAGS += \
    -DRT_STACK_SIZE=${STACK_SIZE} -DRT_RAM_START=${RAM_START}       \
    -DRT_STACK_ADDR=${STACK_ADDR} 									\
    -target mips-none -march=mips2 -mno-madd4 -mno-msa -mno-mt      \
    -mno-virt -mno-xgot -mno-dsp -mno-dspr2 -mabicalls -msoft-float \
    -mllvm -disable-mips-delay-filler=true -mno-gpopt -mno-crc      \
    -nostdlib
ifeq (${ENABLE_MALLOC},true)
override BASE_FLAGS += -DRT_ENABLE_MALLOC
endif
override CFLAGS += \
    ${BASE_FLAGS} -fdata-sections -ffunction-sections -Ilibrt
override ASMFLAGS += \
	${BASE_FLAGS} -DRT_GP=0
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
init.bin: build.bin
	(printf 'ibin';												  \
	 echo '5000 0000' | xxd -r -ps;               				  \
	 printf '%08x' $$(stat --printf='%s' build.bin) | xxd -r -ps; \
     echo '0000 0000' | xxd -r -ps;								  \
	 cat build.bin												  \
	) > init.bin


elf: build.elf
binary: build.bin

clean:
	rm -rf *.o *.elf *.bin
	make -C $(LIBRT_DIR) clean

LIBRT_DIR=librt
LIBRT_TGT=librt.a
librt_target:
	make -C $(LIBRT_DIR) AR="$(AR)" AS="$(AS)" CC="$(CC)" CFLAGS="$(CFLAGS)" ASMFLAGS="$(ASMFLAGS)" $(LIBRT_TGT)
