#pragma once

#include "rt/common.h"

#define SPECIAL_COMPONENT_GPU    0
#define SPECIAL_COMPONENT_DRIVE  2
#define SPECIAL_COMPONENT_EEPROM 1

#ifdef __cplusplus
extern "C" {
#endif

/**
 * Finds the Nth component of a given type.
 *
 * The list of all components of the given type is first sorted by
 * address, then the Nth component is returned.
 *
 * This function is equivalent to
 * 
 * ```
 * components = find_all(type)
 * components.sort_by_address()
 * if(which < len(components)):
 *     *dest = components[i].address
 *     return 0
 * else
 *     return 1
 * ```
 *
 * @param type  Type of the component.
 * @param which Index of the wanted component.
 * @param dest  Where to write the component's address.
 *
 * @return 0 on success, anything else on failure.
 */
int rt_find_component(const char* type, int which, address_t* dest);

/**
 * Maps a special component into an address.
 *
 * A special component is one which has custom methods of being accessed.
 *
 * Currently, only `gpu`, `eeprom` and `drive` components have special memory maps.
 *
 * This function shouldn't normally be needed by programs, instead the definitions in
 * `rt/drive.h`, `rt/eeprom.h` and `rt/framebuffer.h` should be more helpful.
 *
 * For other components, see `rt/ffi.h`.
 *
 * @param addr      Where to map the component.
 * @param component Address of the component.
 * @param type      Type of map wanted.
 * @param flags     Additional value with component-specific meaning.
 *
 * @return 0 on success, anything else on failure.
 */
int rt_map_special_component(volatile void* addr, address_t* component, int type, int flags);

#ifdef __cplusplus
}
#endif
