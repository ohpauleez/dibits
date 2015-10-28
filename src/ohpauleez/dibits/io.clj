(ns ohpauleez.dibits.io
  (:require [ohpauleez.dibits.buffer :as buff])
  (:import (ohpauleez.dibits.buffer Buffer)
           (java.nio ByteOrder)
           (io.netty.buffer ;Unpooled
                            ByteBuf
                            ByteBufUtil)
           (io.netty.buffer PooledByteBufAllocator)
           (java.nio ByteBuffer)))


(def original-unchecked *unchecked-math*)
(set! *unchecked-math* true)

(defn default-buffer
  "This creates a Buffer suitable for byte ops,
  and defaults to using Netty's direct buffers from the PooledByteBufAllocator
  Note: No endianess is enforced - you'll get platform/native."
  ([]
   (.directBuffer PooledByteBufAllocator/DEFAULT))
  ([initial-byte-array]
   (if (instance? Buffer initial-byte-array)
     initial-byte-array ;; Caller assumed Byte Arrays, but was operating in-place
     (buff/write-byte-array (.directBuffer PooledByteBufAllocator/DEFAULT) initial-byte-array))))

(extend-protocol buff/Length

  ByteBuf
  (length [t]
    (.readableBytes t))

  ByteBuffer
  (length [t]
    (.remaining t))

  String
  (length [t]
    (.length t))

  nil
  (length [t] 0))

(extend-protocol buff/Buffer

  ByteBuf
  (ensure-little-endian [t]
    (.order t ByteOrder/LITTLE_ENDIAN))
  (ensure-big-endian [t]
    (.order t ByteOrder/BIG_ENDIAN))
  (slice [t index length]
    (.slice t index length))
  (clear [t]
    (.clear t)
    t))

(extend-protocol buff/WriteBuffer

  ByteBuf
  (write-string [t s]
    (ByteBufUtil/writeUtf8 t ^String s)
    t)
  (write-byte-array [t ba]
    (.writeBytes t ^bytes ba)
    t)
  (write-nio-bytes [t byte-buffer]
    (.writeBytes t ^ByteBuffer byte-buffer))
  (write-bytes [t bb]
    (.writeBytes t ^ByteBuf bb)
    t)
  (write-zero [t n]
    (.writeZero t n)
    t)
  (write-byte [t b]
    (.writeByte t b)
    t)
  (write-ubyte [t b]
    (.writeByte t b)
    t)
  (write-short [t s]
    (.writeShort t s)
    t)
  (write-ushort [t s]
    (.writeShort t s)
    t)
  (write-int [t i]
    (.writeInt t i)
    t)
  (write-uint [t i]
    (.writeInt t i)
    t)
  (write-long [t l]
    (.writeLong t l)
    t)
  (write-ulong [t l]
    (.writeLong t l)
    t)
  (writer-index [t]
    (.writerIndex t))
  (move-writer-index [t n]
    (.writerIndex t n)
    t))

(extend-protocol buff/ReadBuffer

  ByteBuf
  (read-string [t n]
    (let [ba (byte-array n)]
      (.readBytes t ba)
      (String. ba "UTF-8")))
  (read-bytes [t n]
    (let [ba (byte-array n)]
      (.readBytes t ba)
      ba))
  (read-byte [t]
    (.readByte t))
  (read-ubyte [t]
    (.readUnsignedByte t))
  (read-short [t]
    (.readShort t))
  (read-ushort [t]
    (.readUnsignedShort t))
  (read-int [t]
    (.readInt t))
  (read-uint [t]
    (.readUnsignedInt t))
  (read-long [t]
    (.readLong t))
  (read-ulong [t]
    (let [ba (byte-array 8)]
      (.readBytes t ba)
      (BigInteger. ba)))
  (readable-bytes [t]
    (.readableBytes t))
  (reset-read-index [t]
    (.readerIndex t 0)
    t))

(extend-protocol buff/ConvertBuffer

  ByteBuf
  (as-byte-array [t]
    (.array t))
  (as-byte-buffer [t]
    ;; Note: This will share content with the underlying Buffer
    (.nioBuffer t)))

(defn slice
  ([t offset]
   (buff/slice t offset (- (buff/length t) offset)))
  ([t offset length]
   (buff/slice t offset length)))

(def io-fns {:byte {:read buff/read-byte :write buff/write-byte}
             :ubyte {:read buff/read-ubyte :write buff/write-ubyte}
             :int8 {:read buff/read-byte :write buff/write-byte}
             :uint8 {:read buff/read-ubyte :write buff/write-ubyte}

             :short {:read buff/read-short :write buff/write-short}
             :ushort {:read buff/read-ushort :write buff/write-ushort}
             :int16 {:read buff/read-short :write buff/write-short}
             :uint16 {:read buff/read-ushort :write buff/write-ushort}

             :int {:read buff/read-int :write buff/write-int}
             :uint {:read buff/read-uint :write buff/write-uint}
             :int32 {:read buff/read-int :write buff/write-int}
             :uint32 {:read buff/read-uint :write buff/write-uint}

             :long {:read buff/read-long :write buff/write-long}
             :ulong {:read buff/read-ulong :write buff/write-ulong}
             :int64 {:read buff/read-long :write buff/write-long}
             :uint64 {:read buff/read-ulong :write buff/write-ulong}

             :two-piece {:read (fn [^Buffer buffer]
                                 (let [n (buff/read-ushort buffer)]
                                   (buff/read-bytes buffer n)))
                         :write (fn [^Buffer buffer x]
                                  (buff/write-ushort buffer (buff/length x))
                                  (if (string? x)
                                    (buff/write-string buffer x)
                                    (buff/write-bytes buffer x))
                                  buffer)}
             :string2 {:read (fn [^Buffer buffer]
                               (let [n (buff/read-ushort buffer)]
                                 (buff/read-string buffer n)))
                       :write (fn [^Buffer buffer ^String x]
                                  (buff/write-ushort buffer (buff/length x))
                                  (buff/write-string buffer x)
                                  buffer)}
             :four-piece {:read (fn [^Buffer buffer]
                                  (let [n (buff/read-uint buffer)]
                                   (buff/read-bytes buffer n)))
                          :write (fn [^Buffer buffer x]
                                  (buff/write-uint buffer (buff/length x))
                                  (if (string? x)
                                    (buff/write-string buffer x)
                                    (buff/write-bytes buffer x))
                                  buffer)}
             :string4 {:read (fn [^Buffer buffer]
                               (let [n (buff/read-uint buffer)]
                                 (buff/read-string buffer n)))
                       :write (fn [^Buffer buffer ^String x]
                                  (buff/write-uint buffer (buff/length x))
                                  (buff/write-string buffer x)
                                  buffer)}})

;; Spec-oriented I/O
;; -------------------
(defn write!
  [buffer spec data]
  ((:write spec) buffer data))

(defn read!
  ([buffer spec]
   (read! spec {}))
  ([buffer spec initial]
   ((:read spec) buffer initial)))

(set! *unchecked-math* original-unchecked)

