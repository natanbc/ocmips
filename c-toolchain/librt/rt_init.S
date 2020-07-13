#ifndef RT_SP
    #error RT_SP not defined
#endif
#ifndef RT_FP
    #error RT_FP not defined
#endif
#ifndef RT_GP
    #error RT_GP not defined
#endif

.section .rodata

_main_returned: .asciiz "ERR_MAIN_RETURNED"

.section .text.entrypoint
__start: .globl __start
    li $sp, RT_SP
    li $fp, RT_FP
    li $gp, RT_GP
    jal main
    li $v0, 7
    la $a0, _main_returned
    syscall

