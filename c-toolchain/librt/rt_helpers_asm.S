.section .text

# shorter functions to save space

.section .text.__write_sync_word
__write_sync_word:
    sw $v0, 0($a0)
    jr $ra

# void rt_sync_framebuffer(volatile framebuffer_t* fb)
.section .text.rt_sync_framebuffer
rt_sync_framebuffer: .globl rt_sync_framebuffer
    li $v0, 1
    j __write_sync_word

# void rt_clear_framebuffer(volatile framebuffer_t* fb)
.section .text.rt_clear_framebuffer
rt_clear_framebuffer: .globl rt_clear_framebuffer
    li $v0, 2
    j __write_sync_word

# void rt_sync_drive(volatile drive_t* drive, int mode)
.section .text.rt_sync_drive
rt_sync_drive: .globl rt_sync_drive
    move $v0, $a1
    j __write_sync_word

# void rt_sync_eeprom(volatile eeprom_t* eeprom, int mode)
.section .text.rt_sync_eeprom
rt_sync_eeprom: .globl rt_sync_eeprom
    move $v0, $a1
    j __write_sync_word

# void rt_call_method(volatile component_method_t* method)
.section .text.rt_call_method
rt_call_method: .globl rt_call_method
    li $v0, 1
    j __write_sync_word
