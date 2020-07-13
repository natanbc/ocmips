ENTRY(__start)

SECTIONS {
    .text ORIGIN(rom): {
        *(.text.entrypoint)
        *(.text)
    } >rom
    .rodata ADDR(.text) + SIZEOF(.text): {
        *(.rodata)
    } >rom
    .data ADDR(.rodata) + SIZEOF(.rodata): {
        *(.data)
    } >rom
    .bss ADDR(.data) + SIZEOF(.data): {
        *(.bss)
    } >rom
    .sdata ADDR(.bss) + SIZEOF(.bss): {
        __sdata_addr = .;
        *(.sdata)
    } >rom
    /DISCARD/ : {
        *(.MIPS.abiflags)
        *(.reginfo)
        *(.got)
        *(.eh_frame)
    } :NONE
}
