(ns ohpauleez.dibits.buffer
  (:refer-clojure :exclude [read-string]))

(defprotocol Buffer
  (ensure-little-endian [t])
  (ensure-big-endian [t])
  (slice [t index length])
  (clear [t]))

(defprotocol WriteBuffer
  (write-string [t s] [t s pos]) ;; Expected to be UTF-8
  (write-byte-array [t ba] [t ba pos]) ;; Writing full byte-array data
  (write-nio-bytes [t byte-buffer] [t byte-buffer pos]) ;; Writing full ByteBuffer data
  (write-bytes [t buff] [t buff pos]) ;; Writing a Buffer of the same type
  (write-zero [t n] [t n pos]) ;; Used for padding
  (write-byte [t b] [t b pos]) ;; Write 1
  (write-ubyte [t b] [t b pos]) ;; Write 1
  (write-short [t s] [t s pos]) ;; Write 2
  (write-ushort [t s] [t s pos]) ;; Write 2
  (write-int [t i] [t i pos]) ;; Write 4
  (write-uint [t i] [t i pos]) ;; Write 4
  (write-long [t l] [t l pos]) ;; Write 8
  (write-ulong [t l] [t l pos]) ;; Write 8
  (writer-index [t])
  (move-writer-index [t n]))

(defprotocol ReadBuffer
  (read-string [t n] [t n pos])
  (read-bytes [t n] [t n pos])
  (read-byte [t] [t pos]) ;; Read 1
  (read-ubyte [t] [t pos]) ;; Read 1
  (read-short [t] [t pos]) ;; Read 2
  (read-ushort [t] [t pos]) ;; Read 2
  (read-int [t] [t pos]) ;; Read 4
  (read-uint [t] [t pos]) ;; Read 4
  (read-long [t] [t pos]) ;; Read 8
  (read-ulong [t] [t pos]) ;; Read 8
  (readable-bytes [t])
  (reset-read-index [t]))

(defprotocol ConvertBuffer
  (as-byte-array [t])
  (as-byte-buffer [t]))

(defprotocol Length
  (length [t])) ;; A protocol form of `count` that can is easily extended

