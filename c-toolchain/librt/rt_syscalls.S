.section .text

# void rt_sleep(int ticks)
.section .text.rt_sleep
rt_sleep: .globl rt_sleep
    li $v0, 1
    syscall
    jr $ra

# void rt_shutdown(int reboot)
.section .text.rt_shutdown
rt_shutdown: .globl rt_shutdown
    li $v0, 2
    syscall
_infinite_loop:
    j _infinite_loop

# int rt_map_special_component(volatile void* addr, address_t* component, int type, int mode)
.section .text.rt_map_special_component
rt_map_special_component: .globl rt_map_special_component
    li $v0, 3
    syscall
    jr $ra

# int rt_unmap(volatile void* addr)
.section .text.rt_unmap
rt_unmap: .globl rt_unmap
    li $v0, 4
    syscall
    li $v0, 0
    jr $ra

# int rt_map_component(volatile component_method_t* addr, address_t* address, const char* method, int max_args);
.section .text.rt_map_component
rt_map_component: .globl rt_map_component
    li $v0, 5
    syscall
    li $v0, 0
    jr $ra

# int rt_find_component(const char* type, int which, address_t* dest);
.section .text.rt_find_component
rt_find_component: .globl rt_find_component
    li $v0, 6
    syscall
    jr $ra

# void rt_bsod(const char* msg)
.section .text.rt_bsod
rt_bsod: .globl rt_bsod
    li $v0, 7
    syscall
_bsod_loop:
    j _bsod_loop

# int rt_pull_signal(void* ret_buf, int ret_buf_size)
.section .text.rt_pull_signal
rt_pull_signal: .globl rt_pull_signal
    li $v0, 8
    syscall
    jr $ra

# void rt_dbg(const char* msg, int type, int val)
.section .text.rt_dbg
rt_dbg: .globl rt_dbg
    li $v0, 9
    syscall
    jr $ra

# int rt_mremap(void* addr, void* target, int size);
.section .text.rt_mremap
rt_mremap: .globl rt_mremap
    li $v0, 10
    syscall
    jr $ra

# int rt_ramsize();
.section .text.rt_ramsize
    li $v0, 11
    syscall
    jr $ra

