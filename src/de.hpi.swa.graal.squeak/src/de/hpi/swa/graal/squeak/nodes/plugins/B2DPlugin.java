package de.hpi.swa.graal.squeak.nodes.plugins;

import java.util.List;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;

import de.hpi.swa.graal.squeak.model.AbstractSqueakObject;
import de.hpi.swa.graal.squeak.model.ClassObject;
import de.hpi.swa.graal.squeak.model.CompiledMethodObject;
import de.hpi.swa.graal.squeak.model.NativeObject;
import de.hpi.swa.graal.squeak.model.PointersObject;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveFactoryHolder;
import de.hpi.swa.graal.squeak.nodes.primitives.AbstractPrimitiveNode;
import de.hpi.swa.graal.squeak.nodes.primitives.SqueakPrimitive;

public final class B2DPlugin extends AbstractPrimitiveFactoryHolder {

    @Override
    public List<? extends NodeFactory<? extends AbstractPrimitiveNode>> getFactories() {
        return B2DPluginFactory.getFactories();
    }

    // primitiveAbortProcessing omitted because it does not seem to be used.

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveAddActiveEdgeEntry")
    protected abstract static class PrimAddActiveEdgeEntryNode extends AbstractPrimitiveNode {

        protected PrimAddActiveEdgeEntryNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doAdd(final PointersObject receiver, final PointersObject edgeEntry) {
            return B2D.primitiveAddActiveEdgeEntry(receiver, edgeEntry);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveAddBezier")
    protected abstract static class PrimAddBezierNode extends AbstractPrimitiveNode {

        protected PrimAddBezierNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"start.isPoint()", "stop.isPoint()", "via.isPoint()"})
        protected static final Object doAdd(final PointersObject receiver, final PointersObject start, final PointersObject stop, final PointersObject via, final long leftFillIndex,
                        final long rightFillIndex) {
            return B2D.primitiveAddBezier(receiver, start, stop, via, leftFillIndex, rightFillIndex);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveAddBezierShape")
    protected abstract static class PrimAddBezierShapeNode extends AbstractPrimitiveNode {

        protected PrimAddBezierShapeNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doAdd(final PointersObject receiver, final AbstractSqueakObject points, final long nSegments, final long fillStyle, final long lineWidth, final long lineFill) {
            return B2D.primitiveAddBezierShape(receiver, points, nSegments, fillStyle, lineWidth, lineFill);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveAddBitmapFill")
    protected abstract static class PrimAddBitmapFillNode extends AbstractPrimitiveNode {

        protected PrimAddBitmapFillNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"xIndex > 0", "origin.isPoint()", "direction.isPoint()", "normal.isPoint()"})
        protected static final Object doAdd(final PointersObject receiver, final PointersObject form, final AbstractSqueakObject cmap, final boolean tileFlag, final PointersObject origin,
                        final PointersObject direction,
                        final PointersObject normal, final long xIndex) {
            return B2D.primitiveAddBitmapFill(receiver, form, cmap, tileFlag, origin, direction, normal, xIndex);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveAddCompressedShape")
    protected abstract static class PrimAddCompressedShapeNode extends AbstractPrimitiveNode {

        protected PrimAddCompressedShapeNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doAdd(final PointersObject receiver, final NativeObject points, final long nSegments, final NativeObject leftFills, final NativeObject rightFills,
                        final NativeObject lineWidths,
                        final NativeObject lineFills, final NativeObject fillIndexList) {
            return B2D.primitiveAddCompressedShape(receiver, points, nSegments, leftFills, rightFills, lineWidths, lineFills, fillIndexList);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveAddGradientFill")
    protected abstract static class PrimAddGradientFillNode extends AbstractPrimitiveNode {

        protected PrimAddGradientFillNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"colorRamp.isBitmap()", "origin.isPoint()", "direction.isPoint()", "normal.isPoint()"})
        protected static final Object doAdd(final PointersObject receiver, final NativeObject colorRamp, final PointersObject origin, final PointersObject direction, final PointersObject normal,
                        final boolean isRadial) {
            return B2D.primitiveAddGradientFill(receiver, colorRamp, origin, direction, normal, isRadial);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveAddLine")
    protected abstract static class PrimAddLineNode extends AbstractPrimitiveNode {

        protected PrimAddLineNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"start.isPoint()", "end.isPoint()"})
        protected static final Object doAdd(final PointersObject receiver, final PointersObject start, final PointersObject end, final long leftFill, final long rightFill) {
            return B2D.primitiveAddLine(receiver, start, end, leftFill, rightFill);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveAddOval")
    protected abstract static class PrimAddOvalNode extends AbstractPrimitiveNode {

        protected PrimAddOvalNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"start.isPoint()", "end.isPoint()"})
        protected static final Object doAdd(final PointersObject receiver, final PointersObject start, final PointersObject end, final long fillIndex, final long width, final long pixelValue32) {
            return B2D.primitiveAddOval(receiver, start, end, fillIndex, width, pixelValue32);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveAddPolygon")
    protected abstract static class PrimAddPolygonNode extends AbstractPrimitiveNode {

        protected PrimAddPolygonNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doAdd(final PointersObject receiver, final AbstractSqueakObject points, final long nSegments, final long fillStyle, final long lineWidth, final long lineFill) {
            return B2D.primitiveAddPolygon(receiver, points, nSegments, fillStyle, lineWidth, lineFill);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveAddRect")
    protected abstract static class PrimAddRectNode extends AbstractPrimitiveNode {

        protected PrimAddRectNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"start.isPoint()", "end.isPoint()"})
        protected static final Object doAdd(final PointersObject receiver, final PointersObject start, final PointersObject end, final long fillIndex, final long width, final long pixelValue32) {
            return B2D.primitiveAddRect(receiver, start, end, fillIndex, width, pixelValue32);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveChangedActiveEdgeEntry")
    protected abstract static class PrimChangedActiveEdgeEntryNode extends AbstractPrimitiveNode {

        protected PrimChangedActiveEdgeEntryNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doChange(final PointersObject receiver, final PointersObject edgeEntry) {
            return B2D.primitiveChangedActiveEdgeEntry(receiver, edgeEntry);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveCopyBuffer")
    protected abstract static class PrimCopyBufferNode extends AbstractPrimitiveNode {

        protected PrimCopyBufferNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"oldBuffer.isIntType()", "newBuffer.isIntType()"})
        protected static final Object doCopy(final PointersObject receiver, final NativeObject oldBuffer, final NativeObject newBuffer) {
            return B2D.primitiveCopyBuffer(receiver, oldBuffer, newBuffer);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveDisplaySpanBuffer")
    protected abstract static class PrimDisplaySpanBufferNode extends AbstractPrimitiveNode {

        protected PrimDisplaySpanBufferNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doDisplay(final PointersObject receiver) {
            return B2D.primitiveDisplaySpanBuffer(receiver);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveDoProfileStats")
    protected abstract static class PrimDoProfileStatsNode extends AbstractPrimitiveNode {

        protected PrimDoProfileStatsNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doProfile(final PointersObject receiver, final boolean aBoolean) {
            return B2D.primitiveDoProfileStats(receiver, aBoolean);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveFinishedProcessing")
    protected abstract static class PrimFinishedProcessingNode extends AbstractPrimitiveNode {

        protected PrimFinishedProcessingNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doCopy(final PointersObject receiver) {
            return B2D.primitiveFinishedProcessing(receiver);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveGetAALevel")
    protected abstract static class PrimGetAALevelNode extends AbstractPrimitiveNode {

        protected PrimGetAALevelNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doGet(final PointersObject receiver) {
            return B2D.primitiveGetAALevel(receiver);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveGetBezierStats")
    protected abstract static class PrimGetBezierStatsNode extends AbstractPrimitiveNode {

        protected PrimGetBezierStatsNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"statsArray.isIntType()", "statsArray.getIntLength() >= 4"})
        protected static final Object doGet(final PointersObject receiver, final NativeObject statsArray) {
            return B2D.primitiveGetBezierStats(receiver, statsArray);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveGetClipRect")
    protected abstract static class PrimGetClipRectNode extends AbstractPrimitiveNode {

        protected PrimGetClipRectNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"rect.size() >= 2"})
        protected static final Object doGet(final PointersObject receiver, final PointersObject rect) {
            return B2D.primitiveGetClipRect(receiver, rect);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveGetCounts")
    protected abstract static class PrimGetCountsNode extends AbstractPrimitiveNode {

        protected PrimGetCountsNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"statsArray.isIntType()", "statsArray.getIntLength() >= 9"})
        protected static final Object doGet(final PointersObject receiver, final NativeObject statsArray) {
            return B2D.primitiveGetCounts(receiver, statsArray);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveGetDepth")
    protected abstract static class PrimGetDepthNode extends AbstractPrimitiveNode {

        protected PrimGetDepthNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doGet(final PointersObject receiver) {
            return B2D.primitiveGetDepth(receiver);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveGetFailureReason")
    protected abstract static class PrimGetFailureReasonNode extends AbstractPrimitiveNode {

        protected PrimGetFailureReasonNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doGet(final PointersObject receiver) {
            return B2D.primitiveGetFailureReason(receiver);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveGetOffset")
    protected abstract static class PrimGetOffsetNode extends AbstractPrimitiveNode {

        protected PrimGetOffsetNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doGet(final PointersObject receiver) {
            return B2D.primitiveGetOffset(receiver);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveGetTimes")
    protected abstract static class PrimGetTimesNode extends AbstractPrimitiveNode {

        protected PrimGetTimesNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"statsArray.isIntType()", "statsArray.getIntLength() >= 9"})
        protected static final Object doGet(final PointersObject receiver, final NativeObject statsArray) {
            return B2D.primitiveGetTimes(receiver, statsArray);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveInitializeBuffer")
    protected abstract static class PrimInitializeBufferNode extends AbstractPrimitiveNode {

        protected PrimInitializeBufferNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"buffer.isIntType()", "hasMinimalSize(buffer)"})
        protected static final Object doInit(final PointersObject receiver, final NativeObject buffer) {
            return B2D.primitiveInitializeBuffer(receiver, buffer);
        }

        protected static final boolean hasMinimalSize(final NativeObject buffer) {
            return buffer.getIntLength() >= B2D.GWMinimalSize;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveInitializeProcessing")
    protected abstract static class PrimInitializeProcessingNode extends AbstractPrimitiveNode {

        protected PrimInitializeProcessingNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doCopy(final PointersObject receiver) {
            return B2D.primitiveInitializeProcessing(receiver);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveMergeFillFrom")
    protected abstract static class PrimMergeFillFromNode extends AbstractPrimitiveNode {

        protected PrimMergeFillFromNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"fillBitmap.isBitmap()"})
        protected static final Object doCopy(final PointersObject receiver, final NativeObject fillBitmap, final PointersObject fill) {
            return B2D.primitiveMergeFillFrom(receiver, fillBitmap, fill);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveNeedsFlush")
    protected abstract static class PrimNeedsFlushNode extends AbstractPrimitiveNode {

        protected PrimNeedsFlushNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doNeed(final PointersObject receiver) {
            return B2D.primitiveNeedsFlush(receiver);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveNeedsFlushPut")
    protected abstract static class PrimNeedsFlushPutNode extends AbstractPrimitiveNode {

        protected PrimNeedsFlushPutNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doNeed(final PointersObject receiver, final boolean aBoolean) {
            return B2D.primitiveNeedsFlushPut(receiver, aBoolean);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveNextActiveEdgeEntry")
    protected abstract static class PrimNextActiveEdgeEntryNode extends AbstractPrimitiveNode {

        protected PrimNextActiveEdgeEntryNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doNext(final PointersObject receiver, final PointersObject edgeEntry) {
            return B2D.primitiveNextActiveEdgeEntry(receiver, edgeEntry);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveNextFillEntry")
    protected abstract static class PrimNextFillEntryNode extends AbstractPrimitiveNode {

        protected PrimNextFillEntryNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doNext(final PointersObject receiver, final PointersObject fillEntry) {
            return B2D.primitiveNextFillEntry(receiver, fillEntry);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveNextGlobalEdgeEntry")
    protected abstract static class PrimNextGlobalEdgeEntryNode extends AbstractPrimitiveNode {

        protected PrimNextGlobalEdgeEntryNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doNext(final PointersObject receiver, final PointersObject edgeEntry) {
            return B2D.primitiveNextGlobalEdgeEntry(receiver, edgeEntry);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveRegisterExternalEdge")
    protected abstract static class PrimRegisterExternalEdgeNode extends AbstractPrimitiveNode {

        protected PrimRegisterExternalEdgeNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doRegister(final PointersObject receiver, final long index, final long initialX, final long initialY, final long initialZ, final long leftFillIndex,
                        final long rightFillIndex) {
            return B2D.primitiveRegisterExternalEdge(receiver, index, initialX, initialY, initialZ, leftFillIndex, rightFillIndex);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveRegisterExternalFill")
    protected abstract static class PrimRegisterExternalFillNode extends AbstractPrimitiveNode {

        protected PrimRegisterExternalFillNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doRegister(final PointersObject receiver, final long index) {
            return B2D.primitiveRegisterExternalFill(receiver, index);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveRenderImage")
    protected abstract static class PrimRenderImageNode extends AbstractPrimitiveNode {

        protected PrimRenderImageNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doRender(final PointersObject receiver, final PointersObject edge, final PointersObject fill) {
            return B2D.primitiveRenderImage(receiver, edge, fill);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveRenderScanline")
    protected abstract static class PrimRenderScanlineNode extends AbstractPrimitiveNode {

        protected PrimRenderScanlineNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doRender(final PointersObject receiver, final PointersObject edge, final PointersObject fill) {
            return B2D.primitiveRenderScanline(receiver, edge, fill);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveSetAALevel")
    protected abstract static class PrimSetAALevelNode extends AbstractPrimitiveNode {

        protected PrimSetAALevelNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doSet(final PointersObject receiver, final long level) {
            return B2D.primitiveSetAALevel(receiver, level);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveSetBitBltPlugin")
    protected abstract static class PrimSetBitBltPluginNode extends AbstractPrimitiveNode {

        protected PrimSetBitBltPluginNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"pluginName.isByteType()"})
        protected static final Object doSet(final ClassObject receiver, final NativeObject pluginName) {
            return B2D.primitiveSetBitBltPlugin(receiver, pluginName);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveSetClipRect")
    protected abstract static class PrimSetClipRectNode extends AbstractPrimitiveNode {

        protected PrimSetClipRectNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"rect.size() >= 2"})
        protected static final Object doSet(final PointersObject receiver, final PointersObject rect) {
            return B2D.primitiveSetClipRect(receiver, rect);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveSetColorTransform")
    protected abstract static class PrimSetColorTransformNode extends AbstractPrimitiveNode {

        protected PrimSetColorTransformNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doSet(final PointersObject receiver, final AbstractSqueakObject transform) {
            return B2D.primitiveSetColorTransform(receiver, transform);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveSetDepth")
    protected abstract static class PrimSetDepthNode extends AbstractPrimitiveNode {

        protected PrimSetDepthNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doSet(final PointersObject receiver, final long depth) {
            return B2D.primitiveSetDepth(receiver, depth);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveSetEdgeTransform")
    protected abstract static class PrimSetEdgeTransformNode extends AbstractPrimitiveNode {

        protected PrimSetEdgeTransformNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization
        protected static final Object doSet(final PointersObject receiver, final AbstractSqueakObject transform) {
            return B2D.primitiveSetEdgeTransform(receiver, transform);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(name = "primitiveSetOffset")
    protected abstract static class PrimSetOffsetNode extends AbstractPrimitiveNode {

        protected PrimSetOffsetNode(final CompiledMethodObject method, final int numArguments) {
            super(method, numArguments);
        }

        @Specialization(guards = {"point.isPoint()"})
        protected static final Object doSet(final PointersObject receiver, final PointersObject point) {
            return B2D.primitiveSetOffset(receiver, point);
        }
    }
}
