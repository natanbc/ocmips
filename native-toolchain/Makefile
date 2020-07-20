TYPE ?= eeprom

define override_default
  $(strip $(if $(findstring $(origin $(1)), default),
    $(2),
    $(if $($(1)),
	  $($(1)),
	  $(2)
	)
  ))
endef

override AR :=      $(call override_default,AR,llvm-ar-10)
override AS :=      $(call override_default,AS,clang-10)
override CC :=      $(call override_default,CC,clang-10)
override CXX :=     $(call override_default,CXX,clang++-10)
override LD :=      $(call override_default,LD,ld.lld-10)
override LLC :=     $(call override_default,LLC,llc-10)
override OBJCOPY := $(call override_default,OBJCOPY,llvm-objcopy-10)
override RUSTC :=   $(call override_default,RUSTC,rustc)

RAM_START=0x50000000
STACK_SIZE=0x8000
STACK_ADDR=0x58000000

.PHONY: clean build init.bin

.DEFAULT_GOAL := build

ifndef PROJECT
build:
	$(error PROJECT not defined)
endif

clean:
ifdef PROJECT
	rm -rf $(BIN_PATH)
endif
	make -C $(LIBRT_DIR) clean

ifdef PROJECT
  PROJECT_NAME=$(shell basename ${PROJECT})
  RESULT=${PROJECT_NAME}.bin
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

  override BASE_FLAGS += \
      -DRT_STACK_SIZE=${STACK_SIZE} -DRT_RAM_START=${RAM_START}       \
      -DRT_STACK_ADDR=${STACK_ADDR}                                   \
      -target mips-none -march=mips2 -mno-madd4 -mno-msa -mno-mt      \
      -mno-virt -mno-xgot -mno-dsp -mno-dspr2 -mabicalls              \
      -mllvm -disable-mips-delay-filler=true -mno-gpopt -mno-crc      \
      -nostdlib
  ifeq (${ENABLE_MALLOC},true)
    override BASE_FLAGS += -DRT_ENABLE_MALLOC
  endif
  override ASMFLAGS += \
  	${BASE_FLAGS} -DRT_GP=0
  override CFLAGS += \
      ${BASE_FLAGS} -fdata-sections -ffunction-sections -Ilibrt
  override CXXFLAGS += \
	  ${CFLAGS}
  override LDFLAGS += \
      --image-base=${BASE} --section-start=.text=${BASE} --gc-sections \
      --script ${LD_SCRIPT} --defsym=BASE_ADDR=${BASE}
  override LLCFLAGS += \
      -march=mips -mcpu=mips2 -disable-mips-delay-filler=true
  override OBJFLAGS += \
      --set-start=${BASE} -S --strip-unneeded -O binary \
	  --set-section-flags .bss=alloc,load,contents
  override RUSTCFLAGS += \
      --emit=llvm-bc --crate-type=staticlib

  LIBS=librt/librt.a

build: ${RESULT}
# no commands

  BIN_PATH=${PROJECT}/build/
  SRC_PATH=${PROJECT}/src/
  INCLUDE_PATH=${PROJECT}/include

  ELF_FILE=${BIN_PATH}${PROJECT_NAME}.elf
  RAW_FILE=${BIN_PATH}${PROJECT_NAME}.bin
  IBIN_FILE=${BIN_PATH}${PROJECT_NAME}.ibin

  override BASE_FLAGS += -I ${INCLUDE_PATH}

  SOURCE_FILES=$(notdir $(wildcard ${SRC_PATH}*.c ${SRC_PATH}*.S ${SRC_PATH}*.rs ${SRC_PATH}*.cpp))

  OBJ_FILES=$(SOURCE_FILES:=.o)
  OBJ_PATHS=$(addprefix ${BIN_PATH}, $(OBJ_FILES))

# build stuff
$(BIN_PATH)%.c.o: $(SRC_PATH)%.c
	$(CC) $(CFLAGS) -c -o $@ $<
$(BIN_PATH)%.S.o: $(SRC_PATH)%.S
	$(AS) $(ASMFLAGS) -c -o $@ $<
$(BIN_PATH)%.rs.o: $(BIN_PATH)%.rs.bc
	$(LLC) $(LLCFLAGS) -filetype=obj -o $@ $<
$(BIN_PATH)%.rs.bc: $(SRC_PATH)%.rs
	$(RUSTC) $(RUSTCFLAGS) -o $@ $<
$(BIN_PATH)%.cpp.o: $(SRC_PATH)%.cpp
	$(CXX) $(CXXFLAGS) -c -o $@ $<

setup:
	mkdir -p $(BIN_PATH)

$(PROJECT_NAME).elf: setup librt_target $(OBJ_PATHS)
	$(LD) $(LDFLAGS) -o $(ELF_FILE) $(LIBS) $(OBJ_PATHS)
$(PROJECT_NAME).bin: $(PROJECT_NAME).elf
	$(OBJCOPY) $(OBJFLAGS) $(ELF_FILE) $(RAW_FILE)

# format is
# 4 byte magic:                         string "ibin"
# 4 byte base address:                  always 0x50000000 here
# 4 byte size of the data to be loaded: size of the raw binary file
# 4 byte offset of the code start:		always 0 here (base.ld puts __start first)
init.bin: $(PROJECT_NAME).bin
ifeq (${TYPE},initbin)
	(printf 'ibin';													\
	 echo '5000 0000' | xxd -r -ps;               	   				\
	 printf '%08x' $$(stat --printf='%s' $(RAW_FILE)) | xxd -r -ps;	\
     echo '0000 0000' | xxd -r -ps;								  	\
	 cat $(RAW_FILE)											 	\
	) > $(IBIN_FILE)
else
	$(error init.bin can only be build for TYPE=initbin projects)
endif

endif # ifdef PROJECT

LIBRT_DIR=librt
LIBRT_TGT=librt.a
librt_target:
	make -C $(LIBRT_DIR) AR="$(AR)" AS="$(AS)" CC="$(CC)" CFLAGS="$(CFLAGS)" ASMFLAGS="$(ASMFLAGS)" $(LIBRT_TGT)
