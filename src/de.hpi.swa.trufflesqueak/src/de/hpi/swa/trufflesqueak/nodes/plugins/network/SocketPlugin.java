/*
 * Copyright (c) 2017-2020 Software Architecture Group, Hasso Plattner Institute
 *
 * Licensed under the MIT License.
 */
package de.hpi.swa.trufflesqueak.nodes.plugins.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeFactory;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.profiles.ConditionProfile;

import de.hpi.swa.trufflesqueak.SqueakLanguage;
import de.hpi.swa.trufflesqueak.exceptions.PrimitiveExceptions.PrimitiveFailed;
import de.hpi.swa.trufflesqueak.image.SqueakImageContext;
import de.hpi.swa.trufflesqueak.model.AbstractSqueakObject;
import de.hpi.swa.trufflesqueak.model.ArrayObject;
import de.hpi.swa.trufflesqueak.model.BooleanObject;
import de.hpi.swa.trufflesqueak.model.NativeObject;
import de.hpi.swa.trufflesqueak.model.NilObject;
import de.hpi.swa.trufflesqueak.model.PointersObject;
import de.hpi.swa.trufflesqueak.nodes.primitives.AbstractPrimitiveFactoryHolder;
import de.hpi.swa.trufflesqueak.nodes.primitives.AbstractPrimitiveNode;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveInterfaces.BinaryPrimitive;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveInterfaces.QuaternaryPrimitive;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveInterfaces.QuinaryPrimitive;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveInterfaces.SenaryPrimitive;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveInterfaces.SeptenaryPrimitive;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveInterfaces.TernaryPrimitive;
import de.hpi.swa.trufflesqueak.nodes.primitives.PrimitiveInterfaces.UnaryPrimitiveWithoutFallback;
import de.hpi.swa.trufflesqueak.nodes.primitives.SqueakPrimitive;
import de.hpi.swa.trufflesqueak.util.LogUtils;
import de.hpi.swa.trufflesqueak.util.NotProvided;

public final class SocketPlugin extends AbstractPrimitiveFactoryHolder {
    protected static final byte[] LOCAL_HOST_NAME = getLocalHostName().getBytes();

    private static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveResolverStatus")
    protected abstract static class PrimResolverStatusNode extends AbstractPrimitiveNode implements UnaryPrimitiveWithoutFallback {
        @Specialization
        protected static long doWork(@SuppressWarnings("unused") final Object receiver) {
            return Resolver.Status.Ready.id();
        }
    }

    @GenerateNodeFactory
    @NodeInfo(cost = NodeCost.NONE)
    @SqueakPrimitive(names = "primitiveInitializeNetwork")
    protected abstract static class PrimInitializeNetworkNode extends AbstractPrimitiveNode implements UnaryPrimitiveWithoutFallback {
        @Specialization
        protected static Object doWork(final Object receiver) {
            return receiver;
        }
    }

    private static SqueakSocket getSocketOrPrimFail(final PointersObject socketHandle) {
        final Object socket = socketHandle.getHiddenObject();
        if (socket instanceof SqueakSocket) {
            return (SqueakSocket) socket;
        } else {
            throw PrimitiveFailed.andTransferToInterpreter();
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveResolverStartNameLookup")
    protected abstract static class PrimResolverStartNameLookupNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        /**
         * Look up the given host name in the Domain Name Server to find its address. This call is
         * asynchronous. To get the results, wait for it to complete or time out and then use
         * primNameLookupResult.
         */
        @Specialization(guards = "hostName.isByteType()")
        protected static final Object doWork(final Object receiver, final NativeObject hostName) {
            try {
                LogUtils.SOCKET.finer(() -> "Starting lookup for host name " + hostName);
                Resolver.startHostNameLookUp(hostName.asStringUnsafe());
            } catch (final UnknownHostException e) {
                LogUtils.SOCKET.log(Level.FINE, "Host name lookup failed", e);
            }
            return receiver;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveResolverStartAddressLookup")
    protected abstract static class PrimResolverStartAddressLookupNode extends AbstractPrimitiveNode implements BinaryPrimitive {

        /**
         * Look up the given host address in the Domain Name Server to find its name. This call is
         * asynchronous. To get the results, wait for it to complete or time out and then use
         * primAddressLookupResult.
         */
        @Specialization(guards = "address.isByteType()")
        protected static final Object doWork(final Object receiver, final NativeObject address) {
            try {
                LogUtils.SOCKET.finer(() -> "Starting lookup for address " + address);
                Resolver.startAddressLookUp(address.getByteStorage());
            } catch (final UnknownHostException e) {
                LogUtils.SOCKET.log(Level.FINE, "Address lookup failed", e);
            }
            return receiver;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveResolverNameLookupResult")
    protected abstract static class PrimResolverNameLookupResultNode extends AbstractPrimitiveNode implements UnaryPrimitiveWithoutFallback {

        /**
         * Return the host address found by the last host name lookup. Returns nil if the last
         * lookup was unsuccessful.
         */
        @Specialization
        protected static final AbstractSqueakObject doWork(@SuppressWarnings("unused") final Object receiver,
                        @Cached final ConditionProfile hasResultProfile,
                        @CachedContext(SqueakLanguage.class) final SqueakImageContext image) {
            final byte[] lastNameLookup = Resolver.lastHostNameLookupResult();
            LogUtils.SOCKET.finer(() -> "Name Lookup Result: " + Resolver.addressBytesToString(lastNameLookup));
            return hasResultProfile.profile(lastNameLookup == null) ? NilObject.SINGLETON : image.asByteArray(lastNameLookup);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveResolverAddressLookupResult")
    protected abstract static class PrimResolverAddressLookupResultNode extends AbstractPrimitiveNode implements UnaryPrimitiveWithoutFallback {
        /**
         * Return the host name found by the last host address lookup. Returns nil if the last
         * lookup was unsuccessful.
         */
        @Specialization
        protected static final AbstractSqueakObject doWork(@SuppressWarnings("unused") final Object receiver,
                        @CachedContext(SqueakLanguage.class) final SqueakImageContext image) {
            final String lastAddressLookup = Resolver.lastAddressLookUpResult();
            LogUtils.SOCKET.finer(() -> ">> Address Lookup Result: " + lastAddressLookup);
            return lastAddressLookup == null ? NilObject.SINGLETON : image.asByteString(lastAddressLookup);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveResolverLocalAddress")
    protected abstract static class PrimResolverLocalAddressNode extends AbstractPrimitiveNode implements UnaryPrimitiveWithoutFallback {
        @Specialization
        protected static final AbstractSqueakObject doWork(@SuppressWarnings("unused") final Object receiver,
                        @CachedContext(SqueakLanguage.class) final SqueakImageContext image) {
            final byte[] address = Resolver.getLoopbackAddress();
            LogUtils.SOCKET.finer(() -> "Local Address: " + Resolver.addressBytesToString(address));
            return image.asByteArray(address);
        }
    }

    @GenerateNodeFactory
    @ImportStatic(SocketPlugin.class)
    @SqueakPrimitive(names = "primitiveResolverHostNameResult")
    protected abstract static class PrimResolverHostNameResultNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization(guards = {"targetString.isByteType()", "targetString.getByteLength() >= LOCAL_HOST_NAME.length"})
        protected static final Object doResult(@SuppressWarnings("unused") final Object receiver, final NativeObject targetString) {
            System.arraycopy(LOCAL_HOST_NAME, 0, targetString.getByteStorage(), 0, LOCAL_HOST_NAME.length);
            return receiver;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveResolverHostNameSize")
    protected abstract static class PrimResolverHostNameSizeNode extends AbstractPrimitiveNode implements UnaryPrimitiveWithoutFallback {
        @Specialization
        protected static final long doSize(@SuppressWarnings("unused") final Object receiver) {
            return LOCAL_HOST_NAME.length;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketLocalPort")
    protected abstract static class PrimSocketLocalPortNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        /** Return the local port for this socket, or zero if no port has yet been assigned. */
        @Specialization
        protected static final long doLocalPort(@SuppressWarnings("unused") final Object receiver, final PointersObject sd) {
            try {
                return getSocketOrPrimFail(sd).getLocalPort();
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Retrieving local port failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketListenWithOrWithoutBacklog")
    protected abstract static class PrimSocketListenWithOrWithoutBacklogNode extends AbstractPrimitiveNode implements QuaternaryPrimitive {
        /**
         * Listen for a connection on the given port. This is an asynchronous call; query the socket
         * status to discover if and when the connection is actually completed.
         */
        @Specialization
        protected static final Object doListen(final Object receiver,
                        final PointersObject sd,
                        final long port,
                        @SuppressWarnings("unused") final NotProvided backlogSize) {
            try {
                getSocketOrPrimFail(sd).listenOn(port, 0L);
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Listen failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
            return receiver;
        }

        /**
         * Set up the socket to listen on the given port. Will be used in conjunction with #accept
         * only.
         */
        @Specialization
        protected static final Object doListen(final Object receiver,
                        final PointersObject sd,
                        final long port,
                        @SuppressWarnings("unused") final long backlogSize) {
            try {
                getSocketOrPrimFail(sd).listenOn(port, backlogSize);
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Listen failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
            return receiver;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketListenOnPortBacklogInterface")
    protected abstract static class PrimSocketListenOnPortBacklogInterfaceNode extends AbstractPrimitiveNode implements QuinaryPrimitive {
        /**
         * Set up the socket to listen on the given port. Will be used in conjunction with #accept
         * only.
         */
        @Specialization(guards = "interfaceAddress.isByteType()")
        protected static final Object doListen(final Object receiver,
                        final PointersObject sd,
                        final long port,
                        @SuppressWarnings("unused") final long backlogSize,
                        @SuppressWarnings("unused") final NativeObject interfaceAddress) {
            try {
                getSocketOrPrimFail(sd).listenOn(port, backlogSize);
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Listen failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
            return receiver;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketSetOptions")
    protected abstract static class PrimSocketSetOptionsNode extends AbstractPrimitiveNode implements QuaternaryPrimitive {
        @Specialization(guards = "option.isByteType()")
        protected static final ArrayObject doSet(@SuppressWarnings("unused") final Object receiver, final PointersObject sd, final NativeObject option, final NativeObject value,
                        @CachedContext(SqueakLanguage.class) final SqueakImageContext image) {
            try {
                return setSocketOption(image, getSocketOrPrimFail(sd), option.asStringUnsafe(), value.asStringUnsafe());
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Set socket option failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }

        @TruffleBoundary
        private static ArrayObject setSocketOption(final SqueakImageContext image, final SqueakSocket socket, final String option, final String value) throws IOException {
            if (socket.supportsOption(option)) {
                socket.setOption(option, value);
                return image.asArrayOfObjects(0L, image.asByteString(value));
            }
            return image.asArrayOfObjects(1L, image.asByteString("0"));
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketConnectToPort")
    protected abstract static class PrimSocketConnectToPortNode extends AbstractPrimitiveNode implements QuaternaryPrimitive {
        @Specialization(guards = "hostAddress.isByteType()")
        protected static final long doConntext(
                        @SuppressWarnings("unused") final Object receiver, final PointersObject sd,
                        final NativeObject hostAddress, final long port) {
            try {
                final SqueakSocket socket = getSocketOrPrimFail(sd);
                final String host = Resolver.addressBytesToString(hostAddress.getByteStorage());
                socket.connectTo(host, (int) port);
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Socket connect failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
            return 0L;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketConnectionStatus")
    protected abstract static class PrimSocketConnectionStatusNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization
        protected static final long doStatus(@SuppressWarnings("unused") final Object receiver, final PointersObject sd) {
            try {
                return getSocketOrPrimFail(sd).getStatus().id();
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Retrieving socket status failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketRemoteAddress")
    protected abstract static class PrimSocketRemoteAddressNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization
        protected static final AbstractSqueakObject doAddress(@SuppressWarnings("unused") final Object receiver, final PointersObject sd,
                        @CachedContext(SqueakLanguage.class) final SqueakImageContext image) {
            try {
                return image.asByteArray(getSocketOrPrimFail(sd).getRemoteAddress());
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Retrieving remote address failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketRemotePort")
    protected abstract static class PrimSocketRemotePortNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization
        protected static final long doRemotePort(@SuppressWarnings("unused") final Object receiver, final PointersObject sd) {
            try {
                return getSocketOrPrimFail(sd).getRemotePort();
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Retrieving remote port failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketGetOptions")
    protected abstract static class PrimSocketGetOptionsNode extends AbstractPrimitiveNode implements TernaryPrimitive {
        /**
         * Get some option information on this socket. Refer to the UNIX man pages for valid SO,
         * TCP, IP, UDP options. In case of doubt refer to the source code. TCP_NODELAY,
         * SO_KEEPALIVE are valid options for example returns an array containing the error code and
         * the option value.
         */
        @Specialization(guards = "option.isByteType()")
        protected static final Object doGetOption(@SuppressWarnings("unused") final Object receiver, final PointersObject sd, final NativeObject option,
                        @CachedContext(SqueakLanguage.class) final SqueakImageContext image) {
            try {
                final String value = getSocketOrPrimFail(sd).getOption(option.asStringUnsafe());
                return image.asArrayOfObjects(0L, image.asByteString(value));
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Retrieving socket option failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketReceiveDataAvailable")
    protected abstract static class PrimSocketReceiveDataAvailableNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization
        protected static final boolean doDataAvailable(@SuppressWarnings("unused") final Object receiver, final PointersObject sd) {
            try {
                return getSocketOrPrimFail(sd).isDataAvailable();
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Checking for available data failed", e);
                return BooleanObject.FALSE;
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketError")
    protected abstract static class PrimSocketErrorNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization
        @SuppressWarnings("unused")
        protected static long doWork(final Object receiver, final PointersObject sd) {
            return 0L;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketLocalAddress")
    protected abstract static class PrimSocketLocalAddressNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization
        protected static final AbstractSqueakObject doLocalAddress(@SuppressWarnings("unused") final Object receiver, final PointersObject sd,
                        @CachedContext(SqueakLanguage.class) final SqueakImageContext image) {
            try {
                return image.asByteArray(getSocketOrPrimFail(sd).getLocalAddress());
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Retrieving local address failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketSendDataBufCount")
    protected abstract static class PrimSocketSendDataBufCountNode extends AbstractPrimitiveNode implements QuinaryPrimitive {
        /**
         * Send data to the remote host through the given socket starting with the given byte index
         * of the given byte array. The data sent is 'pushed' immediately. Return the number of
         * bytes of data actually sent; any remaining data should be re-submitted for sending after
         * the current send operation has completed. Note: In general, it many take several sendData
         * calls to transmit a large data array since the data is sent in send-buffer-sized chunks.
         * The size of the send buffer is determined when the socket is created.
         */
        @Specialization(guards = "buffer.isByteType()")
        protected static final long doCount(
                        @SuppressWarnings("unused") final Object receiver,
                        final PointersObject sd,
                        final NativeObject buffer,
                        final long startIndex,
                        final long count) {

            try {
                return getSocketOrPrimFail(sd).sendData(buffer.getByteStorage(), (int) startIndex - 1, (int) count);
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Sending data failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketCloseConnection")
    protected abstract static class PrimSocketCloseConnectionNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization
        protected static final Object doClose(final Object receiver, final PointersObject sd) {
            try {
                getSocketOrPrimFail(sd).close();
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Closing socket failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
            return receiver;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketAbortConnection")
    protected abstract static class PrimSocketAbortConnectionNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization
        protected static final Object doAbort(final Object receiver, final PointersObject sd) {
            try {
                getSocketOrPrimFail(sd).close();
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Closing socket failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
            return receiver;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketSendDone")
    protected abstract static class PrimSocketSendDoneNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization
        protected static final Object doSendDone(@SuppressWarnings("unused") final Object receiver, final PointersObject sd) {
            try {
                return BooleanObject.wrap(getSocketOrPrimFail(sd).isSendDone());
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Checking completed send failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketReceiveDataBufCount")
    protected abstract static class PrimSocketReceiveDataBufCountNode extends AbstractPrimitiveNode implements QuinaryPrimitive {
        /**
         * Receive data from the given socket into the given array starting at the given index.
         * Return the number of bytes read or zero if no data is available.
         */
        @Specialization(guards = "buffer.isByteType()")
        protected static final long doCount(
                        @SuppressWarnings("unused") final Object receiver, final PointersObject sd,
                        final NativeObject buffer, final long startIndex, final long count) {
            try {
                return getSocketOrPrimFail(sd).receiveData(buffer.getByteStorage(), (int) startIndex - 1, (int) count);
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Receiving data failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketDestroy")
    protected abstract static class PrimSocketDestroyNode extends AbstractPrimitiveNode implements BinaryPrimitive {
        @Specialization
        protected static final long doDestroy(@SuppressWarnings("unused") final Object receiver, final PointersObject sd) {
            try {
                getSocketOrPrimFail(sd).close();
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Destroying socket failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
            return 0L;
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketCreate3Semaphores")
    protected abstract static class PrimSocketCreate3SemaphoresNode extends AbstractPrimitiveNode implements SeptenaryPrimitive {
        @SuppressWarnings("unused")
        @Specialization
        protected static final PointersObject doWork(final PointersObject receiver,
                        final long netType,
                        final long socketType,
                        final long rcvBufSize,
                        final long semaphoreIndex,
                        final long aReadSemaphore,
                        final long aWriteSemaphore,
                        @Cached final ConditionProfile socketTypeProfile,
                        @CachedContext(SqueakLanguage.class) final SqueakImageContext image) {

            final SqueakSocket socket;
            try {
                if (socketTypeProfile.profile(socketType == 1)) {
                    socket = new SqueakUDPSocket();
                } else {
                    assert socketType == 0;
                    socket = new SqueakTCPSocket();
                }
            } catch (final IOException e) {
                throw PrimitiveFailed.andTransferToInterpreter();
            }
            return PointersObject.newHandleWithHiddenObject(image, socket);
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketAccept3Semaphores")
    protected abstract static class PrimSocketAccept3SemaphoresNode extends AbstractPrimitiveNode implements SeptenaryPrimitive {
        @SuppressWarnings("unused")
        @Specialization
        protected static final PointersObject doWork(final Object receiver,
                        final PointersObject sd,
                        final long receiveBufferSize,
                        final long sendBufSize,
                        final long semaphoreIndex,
                        final long readSemaphoreIndex,
                        final long writeSemaphoreIndex,
                        @CachedContext(SqueakLanguage.class) final SqueakImageContext image) {
            try {
                return PointersObject.newHandleWithHiddenObject(image, getSocketOrPrimFail(sd).accept());
            } catch (final IOException e) {
                LogUtils.SOCKET.log(Level.FINE, "Accepting socket failed", e);
                throw PrimitiveFailed.andTransferToInterpreter();
            }
        }
    }

    @GenerateNodeFactory
    @SqueakPrimitive(names = "primitiveSocketCreate")
    protected abstract static class PrimSocketCreateNode extends AbstractPrimitiveNode implements SenaryPrimitive {
        @SuppressWarnings("unused")
        @Specialization
        protected static long doWork(final PointersObject receiver,
                        final long netType,
                        final long socketType,
                        final long rcvBufSize,
                        final long sendBufSize,
                        final long semaphoreIndex) {
            // TODO: primitiveSocketCreate
            throw PrimitiveFailed.andTransferToInterpreter();
        }

    }

    @Override
    public List<? extends NodeFactory<? extends AbstractPrimitiveNode>> getFactories() {
        return SocketPluginFactory.getFactories();
    }
}
