(ns sky-deck.mutations
  (:require [clojure.spec.alpha :as s]
            [buddy.hashers :as hashers]
            [clojure.string :as str]
            [medley.core :as m]
            [honeysql-postgres.format]
            [honeysql-postgres.helpers])
  (:import (java.util UUID)))

(defn new-id
  []
  (UUID/randomUUID))

(s/def ::id uuid?)

(s/def ::text (s/and string? (complement str/blank?)))
(s/def ::pos-number? number?)

(s/def ::username ::text)
(s/def ::email ::text)
(s/def ::password ::text)

(s/def ::person-inputs
  (s/keys :req-un [::username
                   ::email
                   ::password]))

(s/def ::person-args
  (s/keys :req-un [::person-inputs]
          :opt-un [::new-id]))

(defn generate-person
  [{:keys [new-id person-inputs]
    :as   _person-args}]
  {:insert-into :person
   :values      [(m/assoc-some
                   (update person-inputs :password hashers/derive)
                   :id new-id)]
   :returning   [:*]})

(s/fdef generate-person
        :args (s/cat :person-args ::person-args)
        :ret map?)

(s/def ::description ::text)
(s/def ::dungeon_master_id ::id)

(s/def ::campaign-inputs (s/keys :req-un [::dungeon_master_id]))

(s/def ::campaign-args
  (s/keys :req-un [::campaign-inputs]
          :opt-un [::new-id]))

(defn generate-campaign
  [{:keys [new-id campaign-inputs]
    :as   _campaign-args}]
  {:insert-into :campaign
   :values [(m/assoc-some campaign-inputs :id new-id)]
   :returning [:*]})

(s/fdef generate-campaign
        :args (s/cat :campaign-args ::campaign-args)
        :ret map?)

(s/def ::session-args (s/keys :req-un [::campaign-id]
                              :opt-un [::new-id]))

(defn generate-session
  [{:keys [new-id campaign-id]
    :as   _session-args}]
  {:insert-into :session
   :values [(m/assoc-some {:campaign_id campaign-id} :id new-id)]
   :returning [:*]})

(s/fdef generate-session
        :args (s/cat :session-args ::session-args)
        :ret map?)

(s/def ::name string?)
(s/def ::background string?)
(s/def ::hit_point_max number?)
(s/def ::hit_point_current number?)
(s/def ::agility ::pos-number?)
(s/def ::strength ::pos-number?)
(s/def ::mind ::pos-number?)
(s/def ::soul ::pos-number?)
(s/def ::skill_points ::pos-number?)
(s/def ::reputation ::pos-number?)
(s/def ::master_points ::pos-number?)
(s/def ::divinity_points ::pos-number?)
(s/def ::moments ::pos-number?)
(s/def ::past_lives ::pos-number?)
(s/def ::charges ::pos-number?)
(s/def ::age ::pos-number?)
(s/def ::type #{"player" "npc"})

(s/def ::character-inputs
  (s/keys :req-un [::name
                   ::hit_point_max
                   ::hit_point_current
                   ::agility
                   ::strength
                   ::mind
                   ::soul
                   ::skill_points
                   ::reputation
                   ::divinity_points
                   ::moments
                   ::past_lives
                   ::charges
                   ::age
                   ::background]
          :opt-un [::type]))

(s/def ::character-args
  (s/keys :req-un [::character-inputs]
          :opt-un [::new-id]))

(defn generate-character
  [{:keys [new-id character-inputs]}]
  {:insert-into :character
   :values [(m/assoc-some character-inputs
                          :id new-id)]
   :returning [:*]})

(s/fdef generate-character
        :args (s/cat :character-args ::character-args)
        :ret map?)

(s/def ::action-type-inputs (s/keys :req-un [::name ::slug]))

(s/def ::action-type-args (s/keys :req-un [::action-type-inputs]
                                  :opt-un [::new-id]))

(defn generate-action-type
  [{:keys [new-id action-type-inputs]}]
  {:insert-into :action_type
   :values [(m/assoc-some action-type-inputs :id new-id)]
   :returning [:*]})

(s/fdef generate-action-type
        :args (s/cat ::action-type ::action-type-args))

(defn generate-battle
  [{:keys [new-id campaign-id session-id initiated-by-id]}]
  {:insert-into :battle
   :values [(m/assoc-some {:campaign_id campaign-id
                           :session_id session-id}
                          :id new-id
                          :initiated_by_id initiated-by-id)]
   :returning [:*]})


(defn generate-round
  [{:keys [new-id battle-id campaign-id]}]
  {:insert-into :round
   :values [(m/assoc-some {:battle_id battle-id} :id new-id)]
   :returning [:*]})

;+--------------+------------+----------------------------------------+
;| Column       | Type       | Modifiers                              |
;|--------------+------------+----------------------------------------|
;| id           | uuid       |  not null default uuid_generate_v1mc() |
;| round_id     | uuid       |                                        |
;| character_id | uuid       |                                        |
;| battle_id    | uuid       |                                        |
;| state        | hand_state |  default 'open'::hand_state            |
;+--------------+------------+----------------------------------------+

(s/def ::round-id ::id)
(s/def ::character-id ::id)
(s/def ::battle-id ::id)
(s/def ::state #{"open" "closed"})

(s/def ::hand-args (s/keys :req-un [::round-id ::character-id ::battle-id]
                           :opt-un [::state ::new-id]))

(defn generate-hand
  [{:keys [new-id round-id character-id battle-id state]}]
  {:insert-into :hand
   :values [(m/assoc-some {:round_id round-id
                           :character_id character-id
                           :battle_id battle-id}
                          :id new-id
                          :state state)]
   :returning [:*]})

(s/fdef generate-hand
        :args (s/cat :hand-args ::hand-args)
        :ret map?)

