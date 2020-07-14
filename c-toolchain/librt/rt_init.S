#ifndef RT_STACK_SIZE
    #error RT_STACK_SIZE not defined
#endif
#ifndef RT_STACK_ADDR
    #error RT_STACK_ADDR not defined
#endif
#ifndef RT_RAM_START
    #error RT_RAM_START not defined
#endif
#ifndef RT_GP
    #error RT_GP not defined
#endif

.section .rodata

_main_returned: .asciiz "ERR_MAIN_RETURNED"

#if defined(RT_ENABLE_MALLOC)
.section .data
# used by malloc, bootrom/bios code will always have these as 0
rt_heap_start: .globl rt_heap_start
.word 0
rt_heap_size:  .globl rt_heap_size
.word 0
#endif

.section .text.entrypoint
# void __start(int heap_start, int heap_size)
__start: .globl __start

#if defined(RT_ENABLE_MALLOC)
    la $t0, rt_heap_start               # store malloc globals
    sw $a0, 0($t0)
    la $t0, rt_heap_size
    sw $a1, 0($t0)
#endif

    li $v0, 11                          # load ram size
    syscall

    li $a0, RT_STACK_ADDR               # $a0 = where to map
    li $a1, RT_RAM_START-RT_STACK_SIZE  # $a1 = target
    add $a1, $a1, $v0                   # $a1 = heap end - stack size
    li $a2, RT_STACK_SIZE               # $a2 = map size
    li $v0, 10                          # mremap
    syscall
    
    li $sp, RT_STACK_ADDR+RT_STACK_SIZE
    li $fp, 0
    li $gp, RT_GP
    jal main
    li $v0, 7
    li $a0, 0
    la $a1, _main_returned
    syscall

