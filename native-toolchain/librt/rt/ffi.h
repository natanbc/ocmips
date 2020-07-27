#pragma once

#include "rt/common.h"

#define TYPE_INTEGER            1
#define TYPE_FLOAT              2
#define TYPE_STRING             3
#define TYPE_ADDRESS            4
#define TYPE_NULL               5
#define TYPE_SHORT              6
#define TYPE_BOOLEAN            7
#define TYPE_BYTE_ARRAY         8

/* Useful for allocating a buffer for a known amount of return values. */
#define RET_SIZE_INTEGER        (sizeof(component_method_ret_t))
#define RET_SIZE_FLOAT          RET_SIZE_INTEGER
#define RET_SIZE_STRING(maxlen) (sizeof(component_method_ret_t)+align_up((maxlen)+1, 4))
#define RET_SIZE_ADDRESS        (sizeof(component_method_ret_t)+sizeof(address_t))
#define RET_SIZE_NULL           RET_SIZE_INTEGER
#define RET_SIZE_SHORT          RET_SIZE_INTEGER
#define RET_SIZE_BOOLEAN        RET_SIZE_INTEGER
#define RET_SIZE_BYTEARRAY(len) (sizeof(component_method_ret_t)+sizeof(int)+align_up((len), 4))

#ifdef __cplusplus
extern "C" {
#endif

typedef struct {
    /** Length of this array. */
    unsigned int len;
    /** Contents of this array. */
    uint8_t data[];
} byte_array_t;

typedef struct {
    /** Type of this value. */
    int type;
    /** The value, if it fits with in an int or a pointer to it if it doesn't.
     *  Floats should be converted (via eg type punning) to a bitwise equivalent int.
     */
    int value;
} component_method_arg_t;

typedef struct ___ret {
    /** Type of this value. */
    int type;
    /** Representation of this value, following the same conversion rules as arguments. */
    int value;
    /** Next value in the chain. This is a linked list built on the return buffer. */
    struct ___ret* next;
} component_method_ret_t;

typedef struct {
    /** Call word. Writing to it calls the method. */
    int call;
    /** Buffer of return values. If null, nothing is written. */
    component_method_ret_t* ret_buf;
    /** Size of the return values. If 0, nothing is written. */
    int ret_buf_size;
    /** Number of values returned. Read only. */
    int ret_count;
    /** Number of arguments provided. Must be less than the number of arguments
     *  specified when mapping. */
    int argc;
    /** Arguments for the call. */
    component_method_arg_t args[];
} component_method_t;


/**
 * Maps a component's method into memory.
 *
 * @param addr      Where to map the method.
 * @param component Address of the component.
 * @param method    Name of the method.
 * @param max_args  Maximum number of arguments that might be passed to the method.
 *
 * @return 0 on success, anything else on failure.
 */
int rt_map_component(volatile component_method_t* addr, address_t* component, const char* method, int max_args);

/**
 * Calls a method. This is equivalent to `method->call = 1;`.
 *
 * @param method Method to call.
 */
void rt_call_method(volatile component_method_t* method);

/**
 * Pulls a signal from the signal queue, writing it to the provided return
 * buffer.
 *
 * @param ret_buf      Buffer to write the signal name and arguments to.
 * @param ret_buf_size Size of the return buffer.
 *
 * @return -1 if there was no signal queued, the number of arguments written
 *         to the buffer (including the name) otherwise.
 */
int rt_pull_signal(void* ret_buf, int ret_buf_size);

#ifdef __cplusplus
}
#endif

#ifdef __cplusplus

#include <stdarg.h>

class method {
private:
    volatile component_method_t* m;
public:
    /**
     * Maps a method at a given address, and automatically
     * unmaps it when the destructor is called.
     *
     * After calling this, the result of the mapping operation can be
     * checked with the `operator bool()` method of this class.
     *
     * @param addr     Where to map the method.
     * @param c        Address of the component.
     * @param name     Name of the method.
     * @param max_args Maximum number of arguments that might be passed to this method.
     */
    method(int addr, address_t* c, const char* name, int max_args) {
        m = (volatile component_method_t*)addr;
        if(rt_map_component(m, c, name, max_args) != 0) m = nullptr;
    }

    ~method() {
        rt_unmap(m);
    }

    /**
     * Returns whether or not this mapping was created successfully.
     */
    inline operator bool() const {
        return m != nullptr;
    }

    /**
     * Configures this method with a given number of arguments and their
     * types.
     *
     * @param argc Number of arguments.
     * @param ...  `argc` types of the arguments.
     */
    void configure_args(int argc, ...) {
        m->argc = argc;
        va_list args;
        va_start(args, argc);
        for(int i = 0; i < argc; i++) {
            m->args[i].type = va_arg(args, int);
        }
        va_end(args);
    }

    /**
     * Calls this method with the given arguments.
     *
     * Due to how variadic functions work, at least one named argument
     * is required.
     *
     * @param argc Number of arguments passed. Must be smaller or equal
     *             to the value previously passed to configure_args.
     * @param ...  `argc` values for the argumnents. All values must be
     *             ints, converted using the rules stated in component_method_arg_t.
     */
    void operator() (int argc, ...) {
        va_list args;
        va_start(args, argc);
        for(int i = 0; i < argc; i++) m->args[i].value = va_arg(args, int);
        va_end(args);

        rt_call_method(m);
    }

    /**
     * Allows reading and writing to the method's fields directly.
     */
    inline volatile component_method_t* operator->() { return m; }
};

#endif
