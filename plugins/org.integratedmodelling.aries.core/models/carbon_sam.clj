(ns core.models.carbon-vt
	(:refer-clojure :rename {count length}) 
  (:refer modelling :only (defscenario defmodel model measurement classification 
                            categorization ranking numeric-coding binary-coding 
                            identification bayesian namespace-ontology count))
  (:refer aries :only (span)))

;; defines the ontology associated with this namespace, which may or may not exist.
(namespace-ontology carbonService)


;; output and training TODO make it classify the appropriate measurement - buggy for now
(defmodel veg-soil-storage VegetationAndSoilCarbonStorage
	(classification VegetationAndSoilCarbonStorage
						:units "t/ha" 
	  				[300 3200]  VeryHighStorage
	  				[220 300]   HighStorage
	  				[140 220]   ModerateStorage
	  				[70 140]    LowStorage
	  				[0 70]      VeryLowStorage
	  				0           NoStorage))

;; output and training TODO make it classify the appropriate measurement - buggy for now
(defmodel veg-storage VegetationCarbonStorage
	(classification VegetationCarbonStorage
						:units "t/ha" 
	  				[100 2301] VeryHighVegetationStorage
	  				[80 100]   HighVegetationStorage
	  				[60 80]    ModerateVegetationStorage
	  				[30 60]    LowVegetationStorage
	  				[0 30]     VeryLowVegetationStorage
	  				0          NoVegetationStorage)) 				

;; output and training TODO make it classify the appropriate measurement - buggy for now				
(defmodel soil-storage SoilCarbonStorage
		(classification SoilCarbonStorage
						:units    "t/ha" 
	  				[200 820]  VeryHighSoilStorage
	  				[140 200]  HighSoilStorage
	  				[80 140]   ModerateSoilStorage
	  				[40 80]    LowSoilStorage
	  				[0 40]     VeryLowSoilStorage
	  				0          NoSoilStorage))	  			

;; ----------------------------------------------------------------------------------------------
;; Source model
;; ----------------------------------------------------------------------------------------------

;;NB: ARIES defines the "source" of carbon sequestration as areas that are sequestering carbon (traditionally referred
;; to as "sinks" in the field of carbon research).  "Sinks" in ARIES refer to landscape features that deplete the
;; quantity of a carrier (in this case, sequestered CO2) from being available for human use.  These sinks include
;; areas at risk of deforestation or fire.

(defmodel summer-high-winter-low SummerHighWinterLow
		 (classification (ranking habitat:SummerHighWinterLow)
        [44 :>]       VeryHighSOL
        [42 44]       HighSOL
        [40 42]       ModerateSOL
        [38 40]       LowSOL
        [:< 38]       VeryLowSOL))

(defmodel mean-annual-precip MeanAnnualPrecipitation
			(classification (measurement habitat:AnnualPrecipitation "mm")
					[2500 :>]				  VeryHighMeanAnnualPrecipitation
					[1500 2500]				HighMeanAnnualPrecipitation
					[1000 1500]				ModerateMeanAnnualPrecipitation
					[700 1000]				LowMeanAnnualPrecipitation
					[:< 700]			   	VeryLowMeanAnnualPrecipitation))
						
(defmodel soil-CN-ratio SoilCNRatio
			(classification (ranking habitat:SoilCNRatio)
					[25 :>]				VeryHighCNRatio
					[17.5 25]			HighCNRatio
					[10 17.5]			LowCNRatio
					[:< 10]				VeryLowCNRatio))

(defmodel veg-type VegetationType
			(classification (ranking VegType)
					#{1 5 25}			RowCrops
					#{36 37 62}		GrasslandHerbaceous
					63					  Forest
					87						Wetland
					#{61 82}			NoVegetation
          111           OpenWater))

(defmodel veg-soil-sequestration VegetationAndSoilCarbonSequestration
  (classification VegetationAndSoilCarbonSequestration
                  :units      "t/ha*year"
                  [12 30]     VeryHighSequestration
                  [9 12]      HighSequestration
                  [6 9]       ModerateSequestration
                  [3 6]       LowSequestration
                  [0.01 3]    VeryLowSequestration
                  [0 0.01]    NoSequestration))

;; Bayesian source model
(defmodel source CarbonSourceValue   
  (bayesian CarbonSourceValue 
            :import   "aries.core::CarbonSequestrationSam.xdsl"
            :keep     (VegetationAndSoilCarbonSequestration)
            :observed (veg-soil-sequestration)
            :context  (summer-high-winter-low mean-annual-precip soil-CN-ratio veg-type)))

;; ----------------------------------------------------------------------------------------------
;; Sink model
;; ----------------------------------------------------------------------------------------------

;;NB: ARIES defines the "source" of carbon sequestration as areas that are sequestering carbon (traditionally referred
;; to as "sinks" in the field of carbon research).  "Sinks" in ARIES refer to landscape features that deplete the
;; quantity of a carrier (in this case, sequestered CO2) from being available for human use.  These sinks include
;; areas at risk of deforestation or fire.

;; No data here (at least apparently, see what Sam has to say) - use priors unless there's a layer.  Assume the below discretization
;; is in percentages?
;;(defmodel biomass-removal-rate BiomassRemovalRate
;;      (classification (ranking habitat:BiomassRemovalRate)  
;;          [90 :>]       VeryHighRemovalRate
;;          [66 90]       HighRemovalRate
;;          [10 66]       LowRemovalRate
;;          [:< 10]       VeryLowRemovalRate)) 

;;No data here for "biomass residue input," "soil tillage" "biomass removal rate"- use priors

;; no numbers included in the discretization worksheet so the same numbers as the other concepts are used
(defmodel stored-carbon-release StoredCarbonRelease
  (classification StoredCarbonRelease
                  :units      "t/ha*year"
                  [12 300]   VeryHighRelease ;;Ceiling is a very high carbon storage value for the region's forests from Smith et al. (2006).
                  [9 12]      HighRelease
                  [6 9]       ModerateRelease
                  [3 6]       LowRelease
                  [0.01 3]    VeryLowRelease
                  [0 0.01]    NoRelease))

(defmodel sink CarbonSinkValue   
  (bayesian CarbonSinkValue 
            :import   "aries.core::StoredCarbonReleaseSam.xdsl"
            :keep     (StoredCarbonRelease)
            :observed (stored-carbon-release)
            :context  (summer-high-winter-low mean-annual-precip soil-CN-ratio veg-type)))  ;; add biomass-removal-rate if there's supporting data
   	 		
;; ----------------------------------------------------------------------------------------------
;; Use model
;; ----------------------------------------------------------------------------------------------

(defmodel use-simple GreenhouseGasEmissions
  (measurement GreenhouseGasEmissions "t/ha*year"))
 	 					
;; ----------------------------------------------------------------------------------------------
;; Top-level service models
;; ----------------------------------------------------------------------------------------------

(defmodel identification-carbon ClimateStability
  (identification ClimateStability
                  :context (source :as source
                            sink :as sink
                            use-simple :as use)))

(defmodel carbon-flow ClimateStability
  (span CO2Removed
        VegetationAndSoilCarbonSequestration 
        GreenhouseGasEmissions
        StoredCarbonRelease 
        nil
        nil
        :source-threshold   1.0
        :sink-threshold     1.0
        :use-threshold      10.0
        :trans-threshold    nil
        :source-type        :finite
        :sink-type          :finite
        :use-type           :finite
        :benefit-type       :rival
        :rv-max-states      10
        :downscaling-factor 2
        ;;:save-file          (str (System/getProperty "user.home") "/carbon_data.clj")
        :keep (CarbonSequestration StoredCarbonRelease 
               GreenhouseGasEmissions PotentialCarbonMitigationProvision
               PotentialCarbonMitigationUse UsedCarbonMitigation
               UsedCarbonSink SatisfiedCarbonMitigationDemand
               CarbonMitigationSurplus CarbonMitigationDeficit
               DepletedCarbonMitigation DepletedCarbonMitigationDemand)
        :context (source use-simple sink)))
		
;; ----------------------------------------------------------------------------------------------
;; scenarios (evolving)
;; observations that are specifically tagged for a scenario will be picked up automatically
;; instead of the baseline ones.
;; ----------------------------------------------------------------------------------------------

(defscenario ipcc-hadley-a2-incentivized IPCCHadleyA2Incentivized
  "This scenario represents the effects of the Hadley A1 IPCC climate scenario. A12 
  represents a future world of very rapid economic growth, global population that peaks 
  in mid-century and declines thereafter, and rapid introduction of new and more efficient 
  technologies." 
  ;; old growth has been incentivized, so what was late succession is now old growth
  (classification (ranking ecology:SuccessionalStage)
	 		#{5 6 4}    OldGrowth
	 		3           MidSuccession
	 		2           EarlySuccession
	 		1           PoleSuccession
	 		:otherwise  NoSuccession)) 

(defscenario ipcc-hadley-b2-incentivized IPCCHadleyB2Incentivized
  "This scenario represents the effects of the Hadley B1 IPCC climate scenario. The B1 
  world is a convergent world with the same global population as in the A1 storyline but 
  with rapid changes in economic structures toward a service and information economy, with 
  reductions in material intensity, and the introduction of clean and resource-efficient 
  technologies." 
  ;; old growth has been incentivized, so what was late succession is now old growth
  (classification (ranking ecology:SuccessionalStage)
      #{5 6 4}    OldGrowth
	 		3           MidSuccession
	 		2           EarlySuccession
	 		1           PoleSuccession
	 		:otherwise  NoSuccession))  
       
(defscenario ipcc-hadley-a2 IPCCHadleyA2
	  "This scenario represents the effects of the Hadley A2 IPCC climate scenario. A2
     represents a very heterogeneous world with continuously increasing global population 
     and regionally oriented economic growth that is more fragmented and slower than in
     other storylines." 
) 

(defscenario ipcc-hadley-b2 IPCCHadleyB2
	  "This scenario represents the effects of the Hadley B2 IPCC climate scenario. B2 
     is a world in which the emphasis is on local solutions to economic, social, and 
     environmental sustainability, with continuously increasing population (lower than A2)
     and intermediate economic development. " 
) 
		 			

		 			