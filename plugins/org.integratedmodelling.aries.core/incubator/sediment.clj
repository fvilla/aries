(ns core.models.sediment
  (:refer-clojure :rename {count length}) 
  (:refer modelling :only (defscenario defmodel measurement classification categorization ranking numeric-coding binary-coding identification bayesian count))
  (:refer aries :only (span)))

;; This is the model for Puget Sound.

;; ----------------------------------------------------------------------------------------------
;; Source model
;; ----------------------------------------------------------------------------------------------

(defmodel soil-group 'soilretentionEcology:HydrologicSoilsGroup
	"Relevant soil group"
	(classification (ranking 'habitat:HydrologicSoilsGroup)
			1       'soilretentionEcology:SoilGroupA
			2       'soilretentionEcology:SoilGroupB
			3       'soilretentionEcology:SoilGroupC
			4       'soilretentionEcology:SoilGroupD))

(defmodel slope 'soilretentionEcology:SlopeClass
		(classification (measurement 'geophysics:DegreeSlope "\u00b0")
			 [0 1.15] 	  'soilretentionEcology:Level
			 [1.15 4.57] 	'soilretentionEcology:GentlyUndulating
			 [4.57 16.70] 'soilretentionEcology:RollingToHilly
			 [16.70 :>] 	'soilretentionEcology:SteeplyDissectedToMountainous))

(defmodel slope-stability 'soilretentionEcology:SlopeStabilityClass
    (classification (numeric-coding 'habitat:SlopeStability)	 		
      1           'soilretentionEcology:HighSlopeStability
	 		2           'soilretentionEcology:ModerateSlopeStability
	 		3           'soilretentionEcology:LowSlopeStability))

;;This discretization is for SSURGO/STATSGO, paying attention to texture over inclusion of various sized rock fragments.
(defmodel soil-texture 'soilretentionEcology:SoilTextureClass
    (classification (numeric-coding 'habitat:SoilTexture)
      #{2 3 8 9 12 13 15 17 18 19 20 21 22 25 26 27 29 31 32 34 35 36 37 39 40 43 47 48 50 55 59 62 64 65 66 67 68 69 73 74 75 76 78 79 81 82 84 85 86 87 88 89 91 92 96 98 99 105} 'soilretentionEcology:CoarseSoilTexture
      #{1 4 5 6 10 11 14 24 28 30 33 38 42 49 57 60 61 63 70 71 72 77 80 83 90 93 94 95 97 102 103 104} 'soilretentionEcology:MediumSoilTexture
      #{7 16 23 41 44 45 46 51 52 53 54 56 58 100 101} 'soilretentionEcology:FineSoilTexture))


;;Soil erodibility factor from USLE (unitless).
(defmodel soil-erodibility 'soilretentionEcology:SoilErodibilityClass
     (classification (numeric-coding 'habitat:SoilErodibility)
       [:< 0.1]    'soilretentionEcology:VeryLowSoilErodibility
       [0.1 0.225]   'soilretentionEcology:LowSoilErodibility
       [0.225 0.3]   'soilretentionEcology:ModerateSoilErodibility
       [0.3 0.375]   'soilretentionEcology:HighSoilErodibility
       [0.375 :>]     'soilretentionEcology:VeryHighSoilErodibility))

(defmodel precipitation-annual 'soilretentionEcology:AnnualPrecipitationClass
	"FIXME this is annual precipitation."
	(classification (measurement 'habitat:AnnualPrecipitation "mm")
    [:< 600] 	    'soilretentionEcology:VeryLowAnnualPrecipitation
		[600 1200] 	  'soilretentionEcology:LowAnnualPrecipitation
		[1200 1800]   'soilretentionEcology:ModerateAnnualPrecipitation
		[1800 2200] 	'soilretentionEcology:HighAnnualPrecipitation
		[2200 :>] 	  'soilretentionEcology:VeryHighAnnualPrecipitation))

(defmodel runoff 'soilretentionEcology:AnnualRunoffClass
	(classification (measurement 'habitat:AnnualRunoff "mm")
		[0 200] 	    'soilretentionEcology:VeryLowAnnualRunoff
		[200 600] 	  'soilretentionEcology:LowAnnualRunoff
		[600 1200]  	'soilretentionEcology:ModerateAnnualRunoff
		[1200 2400] 	'soilretentionEcology:HighAnnualRunoff
		[2400 :>] 	  'soilretentionEcology:VeryHighAnnualRunoff))

;;CAN'T do a global vegetation type defmodel if classes are different: split this up & use the local
;; vegetation type defmodel into the BN
;;Vegetation type
(defmodel vegetation-type 'soilretentionEcology:VegetationType
	"Just a reclass of the NLCD land use layer"
	(classification (numeric-coding 'nlcd:NLCDNumeric)
		#{41 42 43 71 90 95} 'soilretentionEcology:ForestGrasslandWetland
		#{52 81}             'soilretentionEcology:ShrublandPasture
		#{21 22 23 24 31 82} 'soilretentionEcology:CropsBarrenDeveloped))

;;Discretization based on Quinton et al. (1997)
(defmodel percent-vegetation-cover 'soilretentionEcology:PercentVegetationCoverClass
  (classification (numeric-coding 'habitat:PercentVegetationCover)
    [70 100]  'soilretentionEcology:HighVegetationCover
    [30 70]  'soilretentionEcology:ModerateVegetationCover
    [0 30]  'soilretentionEcology:LowVegetationCover))

(defmodel successional-stage 'soilretentionEcology:SuccessionalStageClass
	 (classification (numeric-coding 'ecology:SuccessionalStage)  
	 		#{5 6}      'soilretentionEcology:OldGrowth
	 		4           'soilretentionEcology:LateSuccession
	 		3           'soilretentionEcology:MidSuccession
      2           'soilretentionEcology:PoleSuccession
	 		1           'soilretentionEcology:EarlySuccession
	 		:otherwise  'soilretentionEcology:NoSuccession))

;;Sediment source value
(defmodel sediment-source-value-annual 'soilretentionEcology:SedimentSourceValueAnnualClass
 (classification (measurement 'soilretentionEcology:SedimentSourceValueAnnualClass "kg/ha")
  		0                          'soilretentionEcology:NoAnnualSedimentSource
  		[:exclusive 0 30000]       'soilretentionEcology:LowAnnualSedimentSource 
  		[30000 100000]             'soilretentionEcology:ModerateAnnualSedimentSource
  		[100000 :>]                'soilretentionEcology:HighAnnualSedimentSource))

;; source bayesian model for Puget Sound   	 
(defmodel source-puget 'soilretentionEcology:SedimentSourceValueAnnual
  (bayesian 'soilretentionEcology:SedimentSourceValueAnnual 
    :import   "aries.core::SedimentSourceValueAdHoc.xdsl"
    :keep     ('soilretentionEcology:SedimentSourceValueAnnualClass) 
    :observed (sediment-source-value-annual) 
    :context  (soil-group slope soil-texture precipitation-annual vegetation-type percent-vegetation-cover 
              successional-stage slope-stability)))

;; Add deterministic model for USLE: Have data for it for the western U.S. and globally.

;; ----------------------------------------------------------------------------------------------
;; Sink model
;; ----------------------------------------------------------------------------------------------

(defmodel reservoirs 'soilretentionEcology:ReservoirsClass 
  (classification (binary-coding 'geofeatures:Reservoir)
      0          'soilretentionEcology:ReservoirAbsent
      :otherwise 'soilretentionEcology:ReservoirPresent))

(defmodel stream-gradient 'soilretentionEcology:StreamGradientClass 
  (classification (measurement 'habitat:StreamGradient "\u00b0")
    [:<   1.15]  'soilretentionEcology:LowStreamGradient
    [1.15 2.86]  'soilretentionEcology:ModerateStreamGradient
    [2.86 :>]    'soilretentionEcology:HighStreamGradient))

(defmodel floodplain-vegetation-cover 'soilretentionEcology:FloodplainVegetationCoverClass 
  (classification (ranking 'habitat:PercentFloodplainVegetationCover)
    [0 20]   'soilretentionEcology:VeryLowFloodplainVegetationCover
    [20 40]  'soilretentionEcology:LowFloodplainVegetationCover
    [40 60]  'soilretentionEcology:ModerateVegetationCover
    [60 80]  'soilretentionEcology:HighFloodplainVegetationCover
    [80 100] 'soilretentionEcology:VeryHighFloodplainVegetationCover))

(defmodel floodplain-width 'soilretentionEcology:FloodplainWidthClass 
  (classification (measurement 'habitat:FloodplainWidth "m")
    [0 350]     'soilretentionEcology:VeryNarrowFloodplain
    [350 800]   'soilretentionEcology:NarrowFloodplain
    [800 1300]  'soilretentionEcology:WideFloodplain
    [1300 :>]   'soilretentionEcology:VeryWideFloodplain))

;;These are arbitrary numbers discretized based on the "low" soil erosion level defined by the US & global datasets, respectively.
;; Have these numbers reviewed by someone knowledgable about sedimentation.
(defmodel sediment-sink-annual 'soilretentionEcology:AnnualSedimentSinkClass 
  (classification (measurement 'soilretentionEcology:AnnualSedimentSink "kg/ha")
       [20000 30000]          'soilretentionEcology:HighAnnualSedimentSink
       [10000 20000]          'soilretentionEcology:ModerateAnnualSedimentSink
       [:exclusive 0 10000]   'soilretentionEcology:LowAnnualSedimentSink
       0                      'soilretentionEcology:NoAnnualSedimentSink)) 

(defmodel sediment-sink-us 'soilretentionEcology:AnnualSedimentSink
  (bayesian 'soilretentionEcology:AnnualSedimentSink    
    :import  "aries.core::SedimentSink.xdsl"
    :keep    ('soilretentionEcology:AnnualSedimentSinkClass)
    :observed (sediment-sink-annual) 
    :context (reservoirs stream-gradient floodplain-vegetation-cover floodplain-width)))


;; ----------------------------------------------------------------------------------------------
;; Use models
;; ----------------------------------------------------------------------------------------------

(defmodel floodplains 'soilretentionEcology:Floodplains
	(classification (categorization 'geofeatures:Floodplain)
			#{"A" "X500"} 'soilretentionEcology:InFloodplain
			:otherwise    'soilretentionEcology:NotInFloodplain))

(defmodel farmland 'soilretentionEcology:Farmland
	"Just a reclass of the regionally appropriate LULC layer"
	(classification (numeric-coding 'nlcd:NLCDNumeric)
		82	       'soilretentionEcology:FarmlandPresent
		:otherwise 'soilretentionEcology:FarmlandAbsent))

;;Use normal dam storage (ac-ft in the U.S. or m^3 in the rest of the world) as a proxy for 
;;hyroelectric generation capacity (use) - in reality dam height & flow are important factors but 
;;we don't have flow data.

;; Need to insert different discretizations for the US and global models
(defmodel hydroelectric-use-level 'soilretentionEcology:HydroelectricUseLevel
  (measurement 'soilretentionEcology:HydroelectricUseLevel "m^3"))

;; Models farmland in the floodplain, the non-Bayesian way (i.e., basic spatial overlap).
(defmodel farmers-deposition-use-puget 'soilretentionEcology:DepositionProneFarmers 
  (ranking 'soilretentionEcology:DepositionProneFarmers
       :context ((ranking 'nlcd:NLCDNumeric :as farmlandpresent)
                 (ranking 'geofeatures:Floodplain :as floodplains))
       :state #(if (and (= (:floodplains %) 1.0)
                        (= (:farmlandpresent %) 82.0))
                    1
                    0))) 

;; Models farmland in regions with erodible soils, the non-Bayesian way (i.e., basic spatial overlap).
(defmodel farmers-erosion-use-puget 'soilretentionEcology:ErosionProneFarmers
  (ranking 'soilretentionEcology:ErosionProneFarmers
       :state #(if (= (:farmlandpresent %) 82.0)
                  (cond (= (:sediment-source-value-annual %) 'soilretentionEcology:ModerateAnnualSedimentSource)
                        1
                        (= (:sediment-source-value-annual %) 'soilretentionEcology:HighAnnualSedimentSource)
                        2
                        :otherwise
                        0)
                  0)
       :context ((ranking 'nlcd:NLCDNumeric :as farmlandpresent))))

;;Still need defmodels for all components of fisheries BNs.  What about deterministic nodes?
;;Need an undiscretization defmodel before this, for the "observed"? In the long run, could take 2 paths:
;; 1) ditch fisheries BNs & use source/use models for actual fisheries
;; 2) use BNs as generalized fisheries impact model.
;;(defmodel fishermen-use-puget 'soilretentionEcology:FishermenUse 
	  ;;(bayesian 'soilretentionEcology:FishermenUse  
	 ;; 	:import   "aries.core::SedimentFishermenUse.xdsl"
	 ;; 	:keep     ('soilretentionEcology:FishermenUse)
	 ;;	 	:context  (lakes rivers coastline coastal-wetlands salmon-spawning-grounds public-access population-density)))

;; ----------------------------------------------------------------------------------------------
;; Dependencies for the flow model
;; ----------------------------------------------------------------------------------------------

(defmodel altitude 'geophysics:Altitude
  (measurement 'geophysics:Altitude "m"))	 								

(defmodel levees 'soilretentionEcology:LeveesClass 
  (classification (binary-coding 'infrastructure:Levee)
      0          'soilretentionEcology:LeveeAbsent
      :otherwise 'soilretentionEcology:LeveePresent))

(defmodel streams 'geofeatures:River
  (binary-coding 'geofeatures:River))

;; ---------------------------------------------------------------------------------------------------	 	 	
;; Top-level service models 
;; ---------------------------------------------------------------------------------------------------	 	 	

(defmodel reservoir-soil-deposition-data 'soilretentionEcology:ReservoirSoilDeposition
   (identification 'soilretentionEcology:ReservoirSoilDeposition 
     :context (
       source-puget
       sediment-sink-us
       hydroelectric-use-level
       levees)))

(defmodel farmland-soil-deposition-data 'soilretentionEcology:FarmlandSoilDeposition
   (identification 'soilretentionEcology:FarmlandSoilDeposition 
     :context (
       source-puget
       sediment-sink-us
       farmers-deposition-use-puget
       levees)))

;;Sediment flow model for recipients of beneficial sedimentation
(defmodel sediment-beneficial 'soilretentionEcology:BeneficialSedimentTransport
  (span 'soilretentionEcology:SedimentTransport
        'soilretentionEcology:SedimentSourceValueAnnualClass 
        'soilretentionEcology:DepositionProneFarmers
        'soilretentionEcology:AnnualSedimentSinkClass 
        nil
        ('geophysics:Altitude 'soilretentionEcology:Floodplains 'soilretentionEcology:LeveesClass 'geofeatures:River) 
        :source-threshold   1000.0
        :sink-threshold     500.0
        :use-threshold      10.0
        :trans-threshold    100.0
        :source-type        :finite
        :sink-type          :finite
        :use-type           :finite
        :benefit-type       :rival
        :rv-max-states      10
        :downscaling-factor 2
        :keep ('soilretentionEcology:MaximumSedimentSource 'soilretentionEcology:MaximumPotentialDeposition 
               'soilretentionEcology:PotentialSedimentDepositionBeneficiaries 'soilretentionEcology:PossibleSedimentFlow
               'soilretentionEcology:PossibleSedimentSource 'soilretentionEcology:PossibleSedimentDepositionBeneficiaries
               'soilretentionEcology:ActualSedimentFlow 'soilretentionEcology:ActualSedimentSource
               'soilretentionEcology:UtilizedDeposition 'soilretentionEcology:ActualSedimentDepositionBeneficiaries
               'soilretentionEcology:UnutilizedSedimentSource 'soilretentionEcology:InaccessibleSedimentDepositionBeneficiaries
               'soilretentionEcology:AbsorbedSedimentFlow 'soilretentionEcology:NegatedSedimentSource
               'soilretentionEcology:LostValuableSediment)
        ;;:save-file          (str (System/getProperty "user.home") "/carbon_data.clj")
        :context (source-puget farmers-deposition-use-puget sediment-sink-us altitude levees streams floodplains)))

;;Sediment flow model for recipients of avoided detrimental sedimentation
(defmodel sediment-detrimental 'soilretentionEcology:DetrimentalSedimentTransport
  (span 'soilretentionEcology:SedimentTransport
        'soilretentionEcology:SedimentSourceValueAnnualClass 
        'soilretentionEcology:DepositionProneFarmers ;;change the beneficiary group as needed
        'soilretentionEcology:AnnualSedimentSinkClass 
        nil
        ('geophysics:Altitude 'soilretentionEcology:Floodplains 'soilretentionEcology:LeveesClass 'geofeatures:River)
        :source-threshold   1000.0
        :sink-threshold     500.0
        :use-threshold      10.0
        :trans-threshold    100.0
        :source-type        :finite
        :sink-type          :finite
        :use-type           :finite
        :benefit-type       :non-rival
        :rv-max-states      10
        :downscaling-factor 2
        :keep ('soilretentionEcology:MaximumSedimentSource 'soilretentionEcology:MaximumPotentialDeposition 
               'soilretentionEcology:PotentialReducedSedimentDepositionBeneficiaries 'soilretentionEcology:PossibleSedimentFlow
               'soilretentionEcology:PossibleSedimentSource 'soilretentionEcology:PossibleReducedSedimentDepositionBeneficiaries
               'soilretentionEcology:ActualSedimentFlow 'soilretentionEcology:ActualSedimentSource
               'soilretentionEcology:UtilizedDeposition 'soilretentionEcology:ActualReducedSedimentDepositionBeneficiaries
               'soilretentionEcology:UnutilizedDeposition 'soilretentionEcology:AbsorbedSedimentFlow
               'soilretentionEcology:NegatedSedimentSource 'soilretentionEcology:BlockedHarmfulSediment)
        ;;:save-file          (str (System/getProperty "user.home") "/carbon_data.clj")
        :context (source-puget farmers-deposition-use-puget sediment-sink-us altitude levees streams floodplains))) ;;change the beneficiary group as needed

;;Sediment flow model for recipients of reduced turbidity
(defmodel sediment-turbidity 'soilretentionEcology:DetrimentalTurbidity
  (span 'soilretentionEcology:SedimentTransport
        'soilretentionEcology:SedimentSourceValueAnnualClass 
        'carbonService:GreenhouseGasEmissions  ;;change the beneficiary group as needed
        'soilretentionEcology:AnnualSedimentSinkClass 
        nil
        ('geophysics:Altitude 'soilretentionEcology:Floodplains 'soilretentionEcology:LeveesClass 'geofeatures:River)
        :source-threshold   1000.0
        :sink-threshold     500.0
        :use-threshold       10.0
        :trans-threshold    nil
        :source-type        :finite
        :sink-type          :finite
        :use-type           :finite
        :benefit-type       :non-rival
        :rv-max-states      10
        :downscaling-factor 2
        :keep ('soilretentionEcology:MaximumSedimentSource 'soilretentionEcology:MaximumPotentialDeposition 
               'soilretentionEcology:PotentialReducedTurbidityBeneficiaries 'soilretentionEcology:PossibleSedimentFlow
               'soilretentionEcology:PossibleSedimentSource 'soilretentionEcology:PossibleReducedTurbidityBeneficiaries
               'soilretentionEcology:ActualSedimentFlow 'soilretentionEcology:ActualSedimentSource
               'soilretentionEcology:UtilizedDeposition 'soilretentionEcology:ActualReducedTurbidityBeneficiaries
               'soilretentionEcology:UnutilizedDeposition 'soilretentionEcology:AbsorbedSedimentFlow 
               'soilretentionEcology:NegatedSedimentSource 'soilretentionEcology:ReducedTurbidity)
        ;;:save-file          (str (System/getProperty "user.home") "/carbon_data.clj")
        :context (source-puget farmers-deposition-use-puget sediment-sink-us altitude levees streams floodplains))) ;;change the beneficiary group as needed
