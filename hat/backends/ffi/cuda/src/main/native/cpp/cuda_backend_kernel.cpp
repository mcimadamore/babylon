/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


#include "cuda_backend.h"


CudaBackend::CudaModule::CudaKernel::CudaKernel(CompilationUnit *program,char * name, const CUfunction function)
        : Kernel(program, name), function(function) {
}

CudaBackend::CudaModule::CudaKernel::~CudaKernel() = default;

CudaBackend::CudaModule::CudaKernel * CudaBackend::CudaModule::CudaKernel::of(const long kernelHandle){
    return reinterpret_cast<CudaKernel *>(kernelHandle);
}
CudaBackend::CudaModule::CudaKernel * CudaBackend::CudaModule::CudaKernel::of(Kernel *kernel){
    return dynamic_cast<CudaKernel *>(kernel);
}

bool CudaBackend::CudaModule::CudaKernel::setArg(KernelArg *arg){
    argslist[arg->idx] = static_cast<void *>(&arg->value);
    return true;
}
bool CudaBackend::CudaModule::CudaKernel::setArg(KernelArg *arg, Buffer *buffer) {
    argslist[arg->idx] = static_cast<void *>(&dynamic_cast<CudaBuffer *>(buffer)->devicePtr);
    return true;
}
