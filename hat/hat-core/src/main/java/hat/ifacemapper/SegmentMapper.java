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
package hat.ifacemapper;

import hat.buffer.Buffer;

import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A segment mapper can project memory segment onto and from class instances.
 * <p>
 * More specifically, a segment mapper can project a backing
 * {@linkplain MemorySegment MemorySegment} into new {@link Record} instances or new
 * instances that implements an interface by means of matching the names of the record
 * components or interface methods with the names of member layouts in a group layout.
 * A segment mapper can also be used in the other direction, where records and interface
 * implementing instances can be used to update a target memory segment. By using any of
 * the {@linkplain #map(Class, Function) map} operations, segment mappers can be
 * used to map between memory segments and additional Java types other than record and
 * interfaces (such as JavaBeans).
 *
 * <p>
 * In short, a segment mapper finds, for each record component or interface method,
 * a corresponding member layout with the same name in the group layout. There are some
 * restrictions on the record component type and the corresponding member layout type
 * (e.g. a record component of type {@code int} can only be matched with a member layout
 * having a carrier type of {@code int.class} (such as {@link ValueLayout#JAVA_INT})).
 * <p>
 * Using the member layouts (e.g. observing offsets and
 * {@link java.nio.ByteOrder byte ordering}), a number of extraction methods are then
 * identified for all the record components or interface methods and these are stored
 * internally in the segment mapper.
 *
 * <h2 id="mapping-kinds">Mapping kinds</h2>
 * <p>
 * Segment mappers can be of two fundamental kinds;
 * <ul>
 *     <li>Record</li>
 *     <li>Interface</li>
 * </ul>
 * <p>
 * The characteristics of the mapper kinds are summarized in the following table:
 *
 * <blockquote><table class="plain">
 * <caption style="display:none">Mapper characteristics</caption>
 * <thead>
 * <tr>
 *     <th scope="col">Mapper kind</th>
 *     <th scope="col">Temporal mode</th>
 *     <th scope="col">Get operations</th>
 *     <th scope="col">Set operations</th>
 *     <th scope="col">Segment access</th>
 * </tr>
 * </thead>
 * <tbody>
 * <tr><th scope="row" style="font-weight:normal">Record</th>
 *     <td style="text-align:center;">Eager</td>
 *     <td style="text-align:center;">Extract all component values from the source segment, build the record</td>
 *     <td style="text-align:center;">Write all component values to the target segment</td>
 *     <td style="text-align:center;">N/A</td></tr>
 * <tr><th scope="row" style="font-weight:normal">Interface</th>
 *     <td style="text-align:center;">Lazy</td>
 *     <td style="text-align:center;">Wrap the source segment into a new interface instance</td>
 *     <td style="text-align:center;">Copy the relevant values from the initial source segment into the target segment</td>
 *     <td style="text-align:center;">via <code>SegmentMapper::segment</code></td></tr>
 * </tbody>
 * </table></blockquote>
 *
 * <h2 id="mapping-records">Mapping Records</h2>
 * <p>
 * The example below shows how to extract an instance of a public
 * <em>{@code Point} record class</em> from a {@link MemorySegment} and vice versa:
 * {@snippet lang = java:
 *
 *  static final GroupLayout POINT = MemoryLayout.structLayout(JAVA_INT.withName("x"), JAVA_INT.withName("y"));
 *  public record Point(int x, int y){}
 *  //...
 *  MemorySegment segment = MemorySegment.ofArray(new int[]{3, 4, 0, 0});
 *
 *  // Obtain a SegmentMapper for the Point record type
 *  SegmentMapper<Point> recordMapper = SegmentMapper.ofRecord(Point.class, POINT);
 *
 *  // Extracts a new Point record from the provided MemorySegment
 *  Point point = recordMapper.get(segment); // Point[x=3, y=4]
 *
 *  // Writes the Point record to another MemorySegment
 *  MemorySegment otherSegment = Arena.ofAuto().allocate(MemoryLayout.sequenceLayout(2, POINT));
 *  recordMapper.setAtIndex(otherSegment, 1, point); // segment: 0, 0, 3, 4
 *}
 * <p>
 * Boxing, widening, narrowing and general type conversion must be explicitly handled by
 * user code. In the following example, the above {@code Point} (using primitive
 * {@code int x} and {@code int y} coordinates) are explicitly mapped to a narrowed
 * point type (instead using primitive {@code byte x} and {@code byte y} coordinates):
 * <p>
 * {@snippet lang = java:
 * public record NarrowedPoint(byte x, byte y) {
 *
 *     static NarrowedPoint fromPoint(Point p) {
 *         return new NarrowedPoint((byte) p.x, (byte) p.y);
 *     }
 *
 *     static Point toPoint(NarrowedPoint p) {
 *         return new Point(p.x, p.y);
 *     }
 *
 * }
 *
 * SegmentMapper<NarrowedPoint> narrowedPointMapper =
 *         SegmentMapper.ofRecord(Point.class, POINT)              // SegmentMapper<Point>
 *         .map(NarrowedPoint.class, NarrowedPoint::fromPoint, NarrowedPoint::toPoint); // SegmentMapper<NarrowedPoint>
 *
 * // Extracts a new NarrowedPoint from the provided MemorySegment
 * NarrowedPoint narrowedPoint = narrowedPointMapper.get(segment); // NarrowedPoint[x=3, y=4]
 *}
 *
 * <h2 id="mapping-interfaces">Mapping Interfaces</h2>
 * <p>
 * Here is another example showing how to extract an instance of a public
 * <em>interface with an external segment</em>:
 * {@snippet lang = java:
 *
 *  static final GroupLayout POINT = MemoryLayout.structLayout(JAVA_INT.withName("x"), JAVA_INT.withName("y"));
 *
 *  public interface Point {
 *       int x();
 *       void x(int x);
 *       int y();
 *       void y(int x);
 *  }
 *
 *  //...
 *
 *  MemorySegment segment = MemorySegment.ofArray(new int[]{3, 4, 0, 0});
 *
 *  SegmentMapper<Point> mapper = SegmentMapper.of(MethodHandles.lookup(), Point.class, POINT);
 *
 *  // Creates a new Point interface instance with an external segment
 *  Point point = mapper.get(segment); // Point[x=3, y=4]
 *  point.x(6); // Point[x=6, y=4]
 *  point.y(8); // Point[x=6, y=8]
 *
 *  MemorySegment otherSegment = Arena.ofAuto().allocate(MemoryLayout.sequenceLayout(2, POINT)); // otherSegment: 0, 0, 0, 0
 *  mapper.setAtIndex(otherSegment, 1, point); // segment: 0, 0, 6, 8
 *}
 * }
 * <p>
 * Boxing, widening, narrowing and general type conversion must be explicitly handled
 * by user code. In the following example, the above {@code PointAccessor} interface
 * (using primitive {@code int x} and {@code int y} coordinates) are explicitly mapped to
 * a narrowed point type (instead using primitive {@code byte x} and
 * {@code byte y} coordinates):
 * <p>
 * {@snippet lang = java:
 * interface NarrowedPointAccessor {
 *    byte x();
 *    void x(byte x);
 *    byte y();
 *    void y(byte y);
 *
 *    static NarrowedPointAccessor fromPointAccessor(PointAccessor pa) {
 *        return new NarrowedPointAccessor() {
 *            @Override public byte x()       { return (byte)pa.x(); }
 *            @Override public void x(byte x) { pa.x(x); }
 *            @Override public byte y()       { return (byte) pa.y();}
 *            @Override public void y(byte y) { pa.y(y); }
 *       };
 *    }
 *
 * }
 *
 * SegmentMapper<NarrowedPointAccessor> narrowedPointMapper =
 *          // SegmentMapper<PointAccessor>
 *           SegmentMapper.ofInterface(MethodHandles.lookup(), PointAccessor.class, POINT)
 *                   // SegmentMapper<NarrowedPointAccessor>
 *                  .map(NarrowedPointAccessor.class, NarrowedPointAccessor::fromPointAccessor);
 *
 * MemorySegment segment = MemorySegment.ofArray(new int[]{3, 4});
 *
 * // Creates a new NarrowedPointAccessor from the provided MemorySegment
 * NarrowedPointAccessor narrowedPointAccessor = narrowedPointMapper.get(segment); // NarrowedPointAccessor[x=3, y=4]
 *
 * MemorySegment otherSegment = Arena.ofAuto().allocate(MemoryLayout.sequenceLayout(2, POINT));
 * narrowedPointMapper.setAtIndex(otherSegment, 1, narrowedPointAccessor); // otherSegment = 0, 0, 3, 4
 *}
 *
 * <h2 id="segment-exposure">Backing segment exposure</h2>
 * <p>
 * Implementations of interfaces that are obtained via segment mappers can be made to
 * reveal the underlying memory segment and memory segment offset. This is useful when
 * modelling structs that are passed and/or received by native calls:
 * <p>
 * {@snippet lang = java:
 * static final GroupLayout POINT = MemoryLayout.structLayout(JAVA_INT.withName("x"), JAVA_INT.withName("y"));
 *
 * public interface PointAccessor {
 *     int x();
 *     void x(int x);
 *     int y();
 *     void y(int x);
 * }
 *
 * static double nativeDistance(MemorySegment pointStruct) {
 *     // Calls a native method
 *     // ...
 * }
 *
 * public static void main(String[] args) {
 *
 *     SegmentMapper<PointAccessor> mapper =
 *             SegmentMapper.of(MethodHandles.lookup(), PointAccessor.class, POINT);
 *
 *     try (Arena arena = Arena.ofConfined()){
 *         // Creates an interface mapper backed by an internal segment
 *         PointAccessor point = mapper.get(arena);
 *         point.x(3);
 *         point.y(4);
 *
 *         // Pass the backing internal segment to a native method
 *         double distance = nativeDistance(mapper.segment(point).orElseThrow()); // 5
 *     }
 *
 * }
 *}
 *
 * <h2 id="formal-mapping">Formal mapping description</h2>
 * <p>
 * Components and layouts are matched with respect to their name and the exact return type and/or
 * the exact parameter types. No widening or narrowing is employed.
 *
 * <h2 id="restrictions">Restrictions</h2>
 * <p>
 * Generic interfaces need to have their generic type parameters (if any)
 * know at compile time. This applies to all extended interfaces recursively.
 * <p>
 * Interfaces and records must not implement (directly and/or via inheritance) more than
 * one abstract method with the same name and erased parameter types. Hence, covariant
 * overriding is not supported.
 *
 * @param <T> the type this mapper converts MemorySegments from and to.
 * @implSpec Implementations of this interface are immutable, thread-safe and
 * <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>.
 * @since 23
 */

// Todo: Map components to MemorySegment (escape hatch)
// Todo: How do we handle "extra" setters for interfaces? They should not append

// Cerializer
// Todo: Check all exceptions in JavaDocs: See TestScopedOperations
// Todo: Consider generating a graphics rendering.
// Todo: Add in doc that getting via an AddressValue will return a MS managed by Arena.global()
// Todo: Provide safe sharing across threads (e.g. implement a special Interface with piggybacking/volatile access)
// Todo: Prevent several variants in a record from being mapped to a union (otherwise, which will "win" when writing?)
// Todo: There seams to be a problem with the ByteOrder in the mapper. See TestJepExamplesUnions
// Todo: Let SegmentMapper::getHandle and ::setHandle return the sharp types (e.g. Point) see MethodHandles::exactInvoker

// Done: The generated interface classes should be @ValueBased
// Done: Python "Pandas" (tables), Tabular access from array, Joins etc. <- TEST
//       -> See TestDataProcessingRecord and TestDataProcessingInterface
// No: ~map() can be dropped in favour of "manual mapping"~
// Done: Interfaces with internal segments should be directly available via separate factory methods
//       -> Fixed via SegmentMapper::create
// Done: Discuss if an exception is thrown in one of the sub-setters... This means partial update of the MS
//       This can be fixed using double-buffering. Maybe provide a scratch segment somehow that tracks where writes
//       has been made (via a separate class BufferedMapper?)
//       -> Fixed via TestInterfaceMapper::doubleBuffered
public interface SegmentMapper<T> {

    /**
     * {@return the type that this mapper is mapping to and from}
     */
    Class<T> type();

    /**
     * {@return the original {@link GroupLayout } that this mapper is using to map
     * record components or interface methods}
     * <p>
     * Composed segment mappers (obtained via either the {@link SegmentMapper#map(Class, Function)}
     * or the {@link SegmentMapper#map(Class, Function)} will still return the
     * group layout from the <em>original</em> SegmentMapper.
     */
    GroupLayout layout();

    BoundSchema<?> boundSchema();
    // Convenience methods

    /**
     * {@return a new instance of type T projected at an internal {@code segment} at
     * offset zero created by means of invoking the provided {@code arena}}
     * <p>
     * Calling this method is equivalent to the following code:
     * {@snippet lang = java:
     *    get(arena.allocate(layout()));
     *}
     *
     * @param arena from which to {@linkplain Arena#allocate(MemoryLayout) allocate} an
     *              internal memory segment.
     * @throws IllegalStateException     if the {@linkplain MemorySegment#scope() scope}
     * associated with the provided segment is not
     * {@linkplain MemorySegment.Scope#isAlive() alive}
     * @throws WrongThreadException      if this method is called from a thread {@code T},
     * such that {@code isAccessibleBy(T) == false}
     * @throws IllegalArgumentException  if the access operation is
     * <a href="MemorySegment.html#segment-alignment">incompatible with the alignment constraint</a>
     * of the {@link #layout()}
     * @throws IndexOutOfBoundsException if
     * {@code layout().byteSize() > segment.byteSize()}
     */



        /*


         See backend_ffi_shared/include/shared.h

         Make sure the final static values below match the #defines
          // hat iface buffer bitz
        // hat iface bffa   bitz
        // 4a7 1face bffa   b175



        struct state{
           long magic1; // MAGIC
           int bits;
           int mode;
           void * vendorPtr; // In OpenCL this points to native OpenCL::Buffer
           long magic2; // MAGIC
        }
         */

    record BufferState(MemorySegment segment, long paddedSize) {
        public static final long alignment = ValueLayout.JAVA_LONG.byteSize();
        // hat iface buffer bitz
        // hat iface bffa   bitz
        // 4a7 1face bffa   b175
        public static final long MAGIC = 0x4a71facebffab175L;
        public static int NONE = 0;
        public static int BIT_HOST_NEW = 1<<0;
        public static int BIT_DEVICE_NEW = 1<<1;
        public static int BIT_HOST_DIRTY = 1<<2;
        public static int BIT_DEVICE_DIRTY = 1<<3;
        public static int BIT_HOST_CHECKED = 1<<4;

        static final MemoryLayout stateMemoryLayout = MemoryLayout.structLayout(
                ValueLayout.JAVA_LONG.withName("magic1"),
                        ValueLayout.JAVA_INT.withName("bits"),
                        ValueLayout.JAVA_INT.withName("unused"),
                        ValueLayout.ADDRESS.withName("vendorPtr"),
                ValueLayout.JAVA_LONG.withName("magic2")
        ).withName("state");

        static long byteSize(){
            return stateMemoryLayout.byteSize();
        }

        static final VarHandle magic1 = stateMemoryLayout.varHandle(
                MemoryLayout.PathElement.groupElement("magic1")
        );
        static final VarHandle bits = stateMemoryLayout.varHandle(
                MemoryLayout.PathElement.groupElement("bits")
        );

        static final VarHandle magic2 = stateMemoryLayout.varHandle(
                MemoryLayout.PathElement.groupElement("magic2")
        );

        static final VarHandle vendorPtr = stateMemoryLayout.varHandle(
                MemoryLayout.PathElement.groupElement("vendorPtr")
        );

        public static long getLayoutSizeAfterPadding(GroupLayout layout) {
            return layout.byteSize() +
                    ((layout.byteSize() % BufferState.alignment) == 0 ? 0 : BufferState.alignment - (layout.byteSize() % BufferState.alignment));
        }

        public static <T> BufferState of(T t) {
            Buffer buffer = (Buffer) Objects.requireNonNull(t);
            MemorySegment s = Buffer.getMemorySegment(buffer);
            return new BufferState(s,s.byteSize()- BufferState.byteSize());
        }


        BufferState setMagic(){
            BufferState.magic1.set(segment, paddedSize, MAGIC);
            BufferState.magic2.set(segment, paddedSize, MAGIC);
            return this;
        }

        public BufferState assignBits(int bits) {
            BufferState.bits.set(segment, paddedSize, bits);
            return this;
        }
        public BufferState and(int bitz) {
            BufferState.bits.set(segment, paddedSize, getBits()&bitz);
            return this;
        }
        public BufferState or(int bitz) {
            BufferState.bits.set(segment, paddedSize, getBits()|bitz);
            return this;
        }

        public BufferState xor(int bitz) {
             // if getBits() = 0b0111 (7) and bitz = 0b0100 (4) xored = 0x0011 3
             // if getBits() = 0b0011 (3) and bitz = 0b0100 (4) xored = 0x0111 7
            BufferState.bits.set(segment, paddedSize, getBits()^bitz);
            return this;
        }

        public BufferState andNot(int bitz) {
            // if getBits() = 0b0111 (7) and bitz = 0b0100 (4) andNot = 0b0111 & 0b1011 = 0x0011 3
            // if getBits() = 0b0011 (3) and bitz = 0b0100 (4) andNot = 0b0011 & 0b1011 = 0x0011 3
            BufferState.bits.set(segment, paddedSize, getBits()&~bitz);
            return this;
        }


        public int getBits() {
            return (Integer) BufferState.bits.get(segment, paddedSize);
        }
        public MemorySegment getVendorPtr(){return (MemorySegment) BufferState.vendorPtr.get(segment, paddedSize);}
        public void setVendorPtr(MemorySegment vendorPtr){BufferState.vendorPtr.set(segment, paddedSize,vendorPtr);}
        public boolean all(int bitz) {
            return (getBits()&bitz)==bitz;
        }
        public boolean any(int bitz) {
            return (getBits()&bitz)!=0;
        }
        public BufferState setHostDirty(boolean dirty) {
            if (dirty){
                or(BIT_HOST_DIRTY);
            }else{
                andNot(BIT_HOST_DIRTY);
            }
            return this;
        }
        public BufferState setHostChecked(boolean checked) {
            if (checked){
                or(BIT_HOST_CHECKED);
            }else{
                andNot(BIT_HOST_CHECKED); // this is wrong we want bits&=!BIT_DEVICE_DIRTY
            }
            return this;
        }
        public BufferState setDeviceDirty(boolean dirty) {
            if (dirty){
                or(BIT_DEVICE_DIRTY);
            }else{
                andNot(BIT_DEVICE_DIRTY); // this is wrong we want bits&=!BIT_DEVICE_DIRTY
            }
            return this;
        }
        public boolean isHostNew() {
            return all(BIT_HOST_NEW);
        }
        public boolean isHostDirty() {
            return all(BIT_HOST_DIRTY);
        }
        public boolean isHostChecked() {
            return all(BIT_HOST_CHECKED);
        }
        public boolean isHostNewOrDirty() {
            return all(BIT_HOST_NEW|BIT_HOST_DIRTY);
        }
        public boolean isDeviceDirty() {
            return all(BIT_DEVICE_DIRTY);
        }
        public BufferState clearHostChecked() {
            return xor(BIT_HOST_CHECKED);
        }
        public BufferState clearDeviceDirty() {
            return xor(BIT_DEVICE_DIRTY);
        }
        public BufferState resetHostDirty() {
            return xor(BIT_HOST_DIRTY);
        }
        public BufferState resetHostNew() {
            return xor(BIT_HOST_NEW);
        }

        public long magic1() {
            return (Long) BufferState.magic1.get(segment, paddedSize);
        }

        public long magic2() {
            return (Long) BufferState.magic2.get(segment, paddedSize);
        }

        public boolean ok() {
            return MAGIC == magic1() && MAGIC == magic2();
        }

        static String paddedString(int bits) {
            String s = Integer.toBinaryString(bits);
            String s32 = "                                  ";
            return s32.substring(0,s32.length()-s.length())+s;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (ok()){
                builder.append("State:ok").append("\n");
                builder.append("State:Bits:").append(paddedString(getBits()));
                if (all(BIT_HOST_DIRTY)){
                    builder.append(",").append("HOST_DIRTY");
                }
                if (all(BIT_DEVICE_DIRTY)){
                    builder.append(",").append("DEVICE_DIRTY");
                }
                if (all(BIT_HOST_NEW)){
                    builder.append(",").append("HOST_NEW");
                }
                var vendorPtr = getVendorPtr();
                builder.append(",").append("VENDOR_PTR:").append(Long.toHexString(vendorPtr.address()));
                builder.append("\n");


            }else{
                builder.append("State: not ok").append("\n");
            }
            return builder.toString();
        }


    }

    default T allocate(Arena arena, BoundSchema<?> boundSchema) {
        if (boundSchema == null) {
            throw new IllegalStateException("No bound Schema provided");
        }
        //System.out.println("Alloc 16 byte aligned layout + 16 bytes padded to next 16 bytes "+byteSize+"=>"+extendedByteSizePaddedTo16Bytes);
        var segment = arena.allocate(BufferState.getLayoutSizeAfterPadding(layout()) + BufferState.byteSize(), BufferState.alignment);
        new BufferState(segment, BufferState.getLayoutSizeAfterPadding(layout())).setMagic().assignBits(BufferState.BIT_HOST_NEW| BufferState.BIT_HOST_DIRTY);
        T returnValue=  get(segment, layout(), boundSchema);
        // Uncomment if you want to check the State
        /*
        State state = State.of(returnValue);
        if (state.ok() &&!state.isDeviceDirty() &&!state.isJavaDirty()){
            System.out.println("OK");
        }else{
            throw new IllegalArgumentException("BAD TAIL");
        }
         */
        return returnValue;
    }

    /**
     * {@return a new instance of type T projected at the provided
     * external {@code segment} at offset zero}
     * <p>
     * Calling this method is equivalent to the following code:
     * {@snippet lang = java:
     *    get(segment, 0L);
     *}
     *
     * @param segment the external segment to be projected to the new instance
     * @throws IllegalStateException     if the {@linkplain MemorySegment#scope() scope}
     *                                   associated with the provided segment is not
     *                                   {@linkplain MemorySegment.Scope#isAlive() alive}
     * @throws WrongThreadException      if this method is called from a thread {@code T},
     *                                   such that {@code isAccessibleBy(T) == false}
     * @throws IllegalArgumentException  if the access operation is
     *                                   <a href="MemorySegment.html#segment-alignment">incompatible with the alignment constraint</a>
     *                                   of the {@link #layout()}
     * @throws IndexOutOfBoundsException if
     *                                   {@code layout().byteSize() > segment.byteSize()}
     */
    default T get(MemorySegment segment) {
        return get(segment, 0L);
    }

    default T get(MemorySegment segment, GroupLayout groupLayout, BoundSchema<?> boundSchema) {
        return get(segment, groupLayout, boundSchema, 0L);
    }


    /**
     * {@return a new sequential {@code Stream} of elements of type T}
     * <p>
     * Calling this method is equivalent to the following code:
     * {@snippet lang = java:
     * segment.elements(layout())
     *     .map(this::get);
     *}
     *
     * @param segment to carve out instances from
     * @throws IllegalArgumentException if {@code layout().byteSize() == 0}.
     * @throws IllegalArgumentException if {@code segment.byteSize() % layout().byteSize() != 0}.
     * @throws IllegalArgumentException if {@code layout().byteSize() % layout().byteAlignment() != 0}.
     * @throws IllegalArgumentException if this segment is
     *                                  <a href="MemorySegment.html#segment-alignment">incompatible with the
     *                                  alignment constraint</a> in the layout of this segment mapper.
     */
    default Stream<T> stream(MemorySegment segment) {
        return segment.elements(layout())
                .map(this::get);
    }


    /**
     * {@return a new instance of type T projected from at provided
     * external {@code segment} at the provided {@code offset}}
     *
     * @param segment the external segment to be projected at the new instance
     * @param offset  from where in the segment to project the new instance
     * @throws IllegalStateException     if the {@linkplain MemorySegment#scope() scope}
     *                                   associated with the provided segment is not
     *                                   {@linkplain MemorySegment.Scope#isAlive() alive}
     * @throws WrongThreadException      if this method is called from a thread {@code T},
     *                                   such that {@code isAccessibleBy(T) == false}
     * @throws IllegalArgumentException  if the access operation is
     *                                   <a href="MemorySegment.html#segment-alignment">incompatible with the alignment constraint</a>
     *                                   of the {@link #layout()}
     * @throws IndexOutOfBoundsException if
     *                                   {@code offset > segment.byteSize() - layout().byteSize()}
     */
    @SuppressWarnings("unchecked")
    default T get(MemorySegment segment, long offset) {
        try {
            return (T) getHandle()
                    .invokeExact(segment, offset);
        } catch (NullPointerException |
                 IndexOutOfBoundsException |
                 WrongThreadException |
                 IllegalStateException |
                 IllegalArgumentException rethrow) {
            throw rethrow;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to invoke getHandle() with " +
                    "segment=" + segment +
                    ", offset=" + offset, e);
        }
    }

    @SuppressWarnings("unchecked")
    default T get(MemorySegment segment, GroupLayout layout, BoundSchema<?> boundSchema, long offset) {
        try {
            return (T) getHandle()
                    .invokeExact(segment, layout, boundSchema, offset);
        } catch (NullPointerException |
                 IndexOutOfBoundsException |
                 WrongThreadException |
                 IllegalStateException |
                 IllegalArgumentException rethrow) {
            throw rethrow;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to invoke getHandle() with " +
                    "segment=" + segment +
                    ", offset=" + offset, e);
        }
    }

    /**
     * Writes the provided instance {@code t} of type T into the provided {@code segment}
     * at offset zero.
     * <p>
     * Calling this method is equivalent to the following code:
     * {@snippet lang = java:
     *    set(segment, 0L, t);
     *}
     *
     * @param segment in which to write the provided {@code t}
     * @param t       instance to write into the provided segment
     * @throws IllegalStateException         if the {@linkplain MemorySegment#scope() scope}
     *                                       associated with this segment is not
     *                                       {@linkplain MemorySegment.Scope#isAlive() alive}
     * @throws WrongThreadException          if this method is called from a thread {@code T},
     *                                       such that {@code isAccessibleBy(T) == false}
     * @throws IllegalArgumentException      if the access operation is
     *                                       <a href="MemorySegment.html#segment-alignment">incompatible with the alignment constraint</a>
     *                                       of the {@link #layout()}
     * @throws IndexOutOfBoundsException     if {@code layout().byteSize() > segment.byteSize()}
     * @throws UnsupportedOperationException if this segment is
     *                                       {@linkplain MemorySegment#isReadOnly() read-only}
     * @throws UnsupportedOperationException if {@code value} is not a
     *                                       {@linkplain MemorySegment#isNative() native} segment
     * @throws IllegalArgumentException      if an array length does not correspond to the
     *                                       {@linkplain SequenceLayout#elementCount() element count} of a sequence layout
     * @throws NullPointerException          if a required parameter is {@code null}
     */
    default void set(MemorySegment segment, T t) {
        set(segment, 0L, t);
    }

    /**
     * Writes the provided {@code t} instance of type T into the provided {@code segment}
     * at the provided {@code index} scaled by the {@code layout().byteSize()}}.
     * <p>
     * Calling this method is equivalent to the following code:
     * {@snippet lang = java:
     *    set(segment, layout().byteSize() * index, t);
     *}
     *
     * @param segment in which to write the provided {@code t}
     * @param index   a logical index, the offset in bytes (relative to the provided
     *                segment address) at which the access operation will occur can be
     *                expressed as {@code (index * layout().byteSize())}
     * @param t       instance to write into the provided segment
     * @throws IllegalStateException         if the {@linkplain MemorySegment#scope() scope}
     *                                       associated with this segment is not
     *                                       {@linkplain MemorySegment.Scope#isAlive() alive}
     * @throws WrongThreadException          if this method is called from a thread {@code T},
     *                                       such that {@code isAccessibleBy(T) == false}
     * @throws IllegalArgumentException      if the access operation is
     *                                       <a href="MemorySegment.html#segment-alignment">incompatible with the alignment constraint</a>
     *                                       of the {@link #layout()}
     * @throws IndexOutOfBoundsException     if {@code offset > segment.byteSize() - layout.byteSize()}
     * @throws UnsupportedOperationException if this segment is
     *                                       {@linkplain MemorySegment#isReadOnly() read-only}
     * @throws UnsupportedOperationException if {@code value} is not a
     *                                       {@linkplain MemorySegment#isNative() native} segment
     * @throws IllegalArgumentException      if an array length does not correspond to the
     *                                       {@linkplain SequenceLayout#elementCount() element count} of a sequence layout
     * @throws NullPointerException          if a required parameter is {@code null}
     */
    // default void setAtIndex(MemorySegment segment, long index, T t) {
    //    set(segment, layout().byteSize() * index, t);
    //  }

    /**
     * Writes the provided instance {@code t} of type T into the provided {@code segment}
     * at the provided {@code offset}.
     *
     * @param segment in which to write the provided {@code t}
     * @param offset  offset in bytes (relative to the provided segment address) at which
     *                this access operation will occur
     * @param t       instance to write into the provided segment
     * @throws IllegalStateException         if the {@linkplain MemorySegment#scope() scope}
     *                                       associated with this segment is not
     *                                       {@linkplain MemorySegment.Scope#isAlive() alive}
     * @throws WrongThreadException          if this method is called from a thread {@code T},
     *                                       such that {@code isAccessibleBy(T) == false}
     * @throws IllegalArgumentException      if the access operation is
     *                                       <a href="MemorySegment.html#segment-alignment">incompatible with the alignment constraint</a>
     *                                       of the {@link #layout()}
     * @throws IndexOutOfBoundsException     if {@code offset > segment.byteSize() - layout.byteSize()}
     * @throws UnsupportedOperationException if
     *                                       this segment is {@linkplain MemorySegment#isReadOnly() read-only}
     * @throws UnsupportedOperationException if
     *                                       {@code value} is not a {@linkplain MemorySegment#isNative() native} segment // Todo: only for pointers
     * @throws IllegalArgumentException      if an array length does not correspond to the
     *                                       {@linkplain SequenceLayout#elementCount() element count} of a sequence layout
     * @throws NullPointerException          if a required parameter is {@code null}
     */
    default void set(MemorySegment segment, long offset, T t) {
        try {
            setHandle()
                    .invokeExact(segment, offset, (Object) t);
        } catch (IndexOutOfBoundsException |
                 WrongThreadException |
                 IllegalStateException |
                 IllegalArgumentException |
                 UnsupportedOperationException |
                 NullPointerException rethrow) {
            throw rethrow;
        } catch (Throwable e) {
            throw new RuntimeException("Unable to invoke setHandle() with " +
                    "segment=" + segment +
                    ", offset=" + offset +
                    ", t=" + t, e);
        }
    }

    // Basic methods

    /**
     * {@return a method handle that returns new instances of type T projected at
     * a provided external {@code MemorySegment} at a provided {@code long} offset}
     * <p>
     * The returned method handle has the following characteristics:
     * <ul>
     *     <li>its return type is {@code T};</li>
     *     <li>it has a leading parameter of type {@code MemorySegment}
     *         corresponding to the memory segment to be accessed</li>
     *     <li>it has a trailing {@code long} parameter, corresponding to
     *         the base offset</li>
     * </ul>
     *
     * @see #get(MemorySegment, long)
     */
    MethodHandle getHandle();

    /**
     * {@return a method handle that writes a provided instance of type T into
     * a provided {@code MemorySegment} at a provided {@code long} offset}
     * <p>
     * The returned method handle has the following characteristics:
     * <ul>
     *     <li>its return type is void;</li>
     *     <li>it has a leading parameter of type {@code MemorySegment}
     *         corresponding to the memory segment to be accessed</li>
     *     <li>it has a following {@code long} parameter, corresponding to
     *         the base offset</li>
     *     <li>it has a trailing {@code T} parameter, corresponding to
     *         the value to set</li>
     * </ul>
     *
     * @see #set(MemorySegment, long, Object)
     */
    MethodHandle setHandle();

    /**
     * {@return a new segment mapper that would apply the provided {@code toMapper} after
     * performing get operations on this segment mapper and that would throw an
     * {@linkplain UnsupportedOperationException} for set operations if this
     * segment mapper is a record mapper}
     * <p>
     * It should be noted that the type R can represent almost any class and is not
     * restricted to records and interfaces.
     * <p>
     * Interface segment mappers returned by this method does not support
     * {@linkplain #set(MemorySegment, Object) set} operations.
     *
     * @param newType  the new type the returned mapper shall use
     * @param toMapper to apply after get operations on this segment mapper
     * @param <R>      the type of the new segment mapper
     */
    <R> SegmentMapper<R> map(Class<R> newType,
                             Function<? super T, ? extends R> toMapper);

    /**
     * {@return the backing segment of the provided {@code source},
     * or, if no backing segment exists, {@linkplain Optional#empty()}}
     * <p>
     * Interfaces obtained from segment mappers have backing segments. Records obtained
     * from segment mappers do not.
     *
     * @param source from which to extract the backing segment
     */
    default Optional<MemorySegment> segment(T source) {
        Objects.requireNonNull(source);
        return Optional.empty();
    }

    /**
     * {@return the offset in the backing segment of the provided {@code source},
     * or, if no backing segment exists, {@linkplain OptionalLong#empty()}}
     * <p>
     * Interfaces obtained from segment mappers have backing segments. Records obtained
     * from segment mappers do not.
     *
     * @param source from which to extract the backing segment
     */
    default OptionalLong offset(T source) {
        Objects.requireNonNull(source);
        return OptionalLong.empty();
    }

    /**
     * {@return a segment mapper that maps {@linkplain MemorySegment memory segments}
     * to the provided interface {@code type} using the provided {@code layout}
     * and using the provided {@code lookup}}
     *
     * @param lookup to use when performing reflective analysis on the
     *               provided {@code type}
     * @param type   to map memory segment from and to
     * @param layout to be used when mapping the provided {@code type}
     * @param <T>    the type the returned mapper converts MemorySegments from and to
     * @throws IllegalArgumentException if the provided {@code type} is not an interface
     * @throws IllegalArgumentException if the provided {@code type} is a hidden interface
     * @throws IllegalArgumentException if the provided {@code type} is a sealed interface
     * @throws IllegalArgumentException if the provided interface {@code type} directly
     *                                  declares any generic type parameter
     * @throws IllegalArgumentException if the provided interface {@code type} cannot be
     *                                  reflectively analysed using the provided {@code lookup}
     * @throws IllegalArgumentException if the provided interface {@code type} contains
     *                                  methods for which there are no exact mapping (of names and types) in
     *                                  the provided {@code layout} or if the provided {@code type} is not public or
     *                                  if the method is otherwise unable to create a segment mapper as specified above
     * @implNote The order in which methods appear (e.g. in the {@code toString} method)
     * is derived from the provided {@code layout}.
     * @implNote The returned class can be a
     * <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
     * class; programmers should treat instances that are
     * {@linkplain Object#equals(Object) equal} as interchangeable and should
     * not use instances for synchronization, or unpredictable behavior may
     * occur. For example, in a future release, synchronization may fail.
     * @implNote The returned class can be a {@linkplain Class#isHidden() hidden} class.
     */
    static <T> SegmentMapper<T> of(MethodHandles.Lookup lookup,
                                   Class<T> type,
                                   GroupLayout layout) {
        Objects.requireNonNull(lookup);
        MapperUtil.requireImplementableInterfaceType(type);
        Objects.requireNonNull(layout);
        return SegmentInterfaceMapper.create(lookup, type, layout, null);
    }

    static <T extends Buffer> SegmentMapper<T> of(MethodHandles.Lookup lookup, Class<T> type, GroupLayout layout, BoundSchema<?> boundSchema) {
        Objects.requireNonNull(lookup);
        MapperUtil.requireImplementableInterfaceType(type);
        Objects.requireNonNull(layout);
        return SegmentInterfaceMapper.create(lookup, type, layout, boundSchema);

    }


    static <T> SegmentMapper<T> of(MethodHandles.Lookup lookup,
                                   Class<T> type,
                                   MemoryLayout... elements) {

        StructLayout structlayout = MemoryLayout.structLayout(elements).withName(type.getSimpleName());
        return of(lookup, type, structlayout);
    }


    /**
     * Interfaces extending this interface will be provided
     * with additional methods for discovering the backing
     * memory segment and offset used as the backing storage.
     */
    interface Discoverable {

        /**
         * {@return the backing segment of this instance}
         */
        MemorySegment segment();

        /**
         * {@return the backing segment of this instance}
         */
        MemoryLayout layout();

        /**
         * {@return the offset in the backing segment of this instance}
         */
        long offset();
    }

}
