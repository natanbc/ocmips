#ifndef RT_SP
    #error RT_SP not defined
#endif
#ifndef RT_FP
    #error RT_FP not defined
#endif
#ifndef RT_GP
    #error RT_GP not defined
#endif

.section .data

_main_returned: .asciiz "ERR_MAIN_RETURNED"

.section .text
__start: .globl __start
    li $sp, RT_SP
    li $fp, RT_FP
    li $gp, RT_GP
    jal main
    li $v0, 7
    la $a0, _main_returned
    syscall

# void rt_sleep(int ticks)
rt_sleep: .globl rt_sleep
    li $v0, 1
    syscall
    jr $ra

# void rt_shutdown(int reboot)
rt_shutdown: .globl rt_shutdown
    li $v0, 2
    syscall
_infinite_loop:
    j _infinite_loop

# int rt_map_special_component(volatile void* addr, address_t* component, int type)
rt_map_special_component: .globl rt_map_special_component
    li $v0, 3
    syscall
    jr $ra

# int rt_unmap(volatile void* addr)
rt_unmap: .globl rt_unmap
    li $v0, 4
    syscall
    li $v0, 0
    jr $ra

# int rt_map_component(volatile component_method_t* addr, address_t* address, const char* method, int max_args);
rt_map_component: .globl rt_map_component
    li $v0, 5
    syscall
    li $v0, 0
    jr $ra

# int rt_find_component(const char* type, int which, address_t* dest);
rt_find_component: .globl rt_find_component
    li $v0, 6
    syscall
    jr $ra

# void rt_bsod(const char* msg)
rt_bsod: .globl rt_bsod
    li $v0, 7
    syscall
_bsod_loop:
    j _bsod_loop
