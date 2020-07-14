#include "malloc.h"

#if defined(RT_ENABLE_MALLOC)

#define ALLOC_HEADER_SZ offsetof(alloc_node_t, block)

#define MIN_ALLOC_SZ ALLOC_HEADER_SZ + 16

#define list_entry(ptr, type, member) container_of(ptr, type, member)

#define list_first_entry(head, type, member)                                   \
    list_entry((head)->next, type, member)


#define list_for_each(pos, head)                                               \
    for(pos = (head)->next; pos != (head); pos = pos->next)

#define list_for_each_safe(pos, n, head)                                       \
    for(pos = (head)->next, n = pos->next; pos != (head);                      \
        pos = n, n = pos->next)

#define list_for_each_entry(pos, head, member)                                 \
    for(pos = list_entry((head)->next, __typeof__(*pos), member);              \
        &pos->member != (head);                                                \
        pos = list_entry(pos->member.next, __typeof__(*pos), member))

#define list_for_each_entry_safe(pos, n, head, member)                         \
    for(pos = list_entry((head)->next, __typeof__(*pos), member),              \
    n = list_entry(pos->member.next, __typeof__(*pos), member);                \
        &pos->member != (head);                                                \
        pos = n, n = list_entry(n->member.next, __typeof__(*n), member))

typedef struct ll_head {
    struct ll_head* next;
    struct ll_head* prev;
} ll_t;

typedef struct {
    ll_t node;
    size_t size;
    char* block;
} alloc_node_t;

static ll_t free_list = {&free_list, &free_list};

extern int rt_heap_start;
extern int rt_heap_size;
extern int rt_does_not_exist;

static inline void list_add_(ll_t* n, ll_t* prev, ll_t* next) {
    next->prev = n;
    n->next = next;
    n->prev = prev;
    prev->next = n;
}

static inline void list_add(ll_t* n, ll_t* head) {
    list_add_(n, head, head->next);
}

static inline void list_add_tail(ll_t* n, ll_t* head) {
    list_add_(n, head->prev, head);
}

static inline void list_del_(ll_t* prev, ll_t* next) {
    next->prev = prev;
    prev->next = next;
}

static inline void list_del(ll_t* entry) {
    list_del_(entry->prev, entry->next);
    entry->next = NULL;
    entry->prev = NULL;
}

static void defrag_free_list(void) {
    alloc_node_t* block;
    alloc_node_t* t;
    alloc_node_t* last_block = NULL;
    list_for_each_entry_safe(block, t, &free_list, node) {
        if(last_block) {
            if((((uintptr_t)&last_block->block) + last_block->size) == (uintptr_t)block) {
                last_block->size += sizeof(*block) + block->size;
                list_del(&block->node);
                continue;
            }
        }
        last_block = block;
    }
}

void rt_malloc_init(void) {
    // no heap :(
    if(rt_heap_start == 0)
        return;
    alloc_node_t* blk = (void*) align_up(rt_heap_start, sizeof(void*));
    blk->size = rt_heap_start + rt_heap_size - (int) blk - ALLOC_HEADER_SZ;

    list_add(&blk->node, &free_list);
}

void* malloc(size_t size) {
    void* ptr = NULL;
    alloc_node_t* blk = NULL;
    
    if(size == 0) return NULL;

    size = align_up(size, sizeof(void*));
    list_for_each_entry(blk, &free_list, node) {
        if(blk->size >= size) {
            ptr = &blk->block;
            break;
        }
    }

    if(ptr) {
        if((blk->size - size) >= MIN_ALLOC_SZ) {
            alloc_node_t* new_blk = (alloc_node_t*)((uintptr_t)(&blk->block) + size);
            new_blk->size = blk->size - size - ALLOC_HEADER_SZ;
            blk->size = size;
            list_add_(&new_blk->node, &blk->node, blk->node.next);
        }
        list_del(&blk->node);
    }

    return (void*) rt_heap_start;
}

void free(void* addr) {
    if(addr == NULL) return;
    alloc_node_t* blk = container_of(addr, alloc_node_t, block);
    alloc_node_t* free_blk;
    list_for_each_entry(free_blk, &free_list, node) {
        if(free_blk > blk) {
            list_add_(&blk->node, free_blk->node.prev, &free_blk->node);
            goto blockadded;
        }
    }
    list_add_tail(&blk->node, &free_list);
blockadded:
    defrag_free_list();
}

#endif
