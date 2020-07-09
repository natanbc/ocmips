package com.github.natanbc.mipscpu.memory;

import com.github.natanbc.mipscpu.MipsCPU;

public interface MemoryHandler {
    /**
     * Called when this handler is attached to a CPU.
     *
     * @param cpu         The CPU this handler was attached to.
     * @param baseAddress The base address this handler was installed at.
     */
    default void onAttach(MipsCPU cpu, int baseAddress) {}

    /**
     * Reads memory from a given address. The address will always be 4-byte aligned.
     *
     * @param cpu     The CPU that triggered the read.
     * @param address The address to be read.
     *
     * @return The value at the given address.
     *
     * @throws IllegalAddressException If this operation is not supported on the given address.
     */
    int read(MipsCPU cpu, int address) throws IllegalAddressException;

    /**
     * Writes to memory on a given address. The address will always be 4-byte aligned.
     *
     * @param cpu     The CPU that triggered the write.
     * @param address The address to be written.
     * @param value   The value to write.
     *
     * @throws IllegalAddressException If this operation is not supported on the given address.
     */
    void write(MipsCPU cpu, int address, int value) throws IllegalAddressException;

    /**
     * @return The size of the memory region controlled by this handler.
     */
    int memorySize();
}
