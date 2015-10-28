(ns ohpauleez.dibits.spec)

(defn spec
  "Specs are kv-pairs,
  where the values are maps that have a :read function and a :write function.
  The functions should match arguments from the Buffer Protocols.  You should
  use `ohpauleez.dibits.io/io-fns` for primitive data types.

  This returns a map of a single :read and :write function."
  [& args]
  {:pre [(even? (count args))]}
  (let [spec-pairs (partition 2 args)
        spec-writers (map (juxt first (comp :write second)) spec-pairs)
        spec-readers (map (juxt first (comp :read second)) spec-pairs)
        write-fn (fn [buffer data]
                   (loop [codecs spec-writers]
                     (when-let [[k writer] (first codecs)]
                       (writer buffer (get data k))
                       (recur (rest codecs))))
                   buffer)
        read-fn (fn [buffer initial]
                  (persistent!
                    (reduce (fn [acc [k reader]]
                              (assoc! acc k (reader buffer)))
                            (transient initial)
                            spec-readers)))]
    {:write write-fn
     :read read-fn}))

(comment

  ;; For example...
  (spec :type (io/io-fns :uint)
        :tag (io/io-fns :ushort))

  )

