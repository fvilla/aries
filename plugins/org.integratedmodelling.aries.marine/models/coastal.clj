;; --------------------------------------------------------------------------------------------------
;; UNEP marine project
;; models for coastal protection
;; fv Jan 10
;; --------------------------------------------------------------------------------------------------

(ns aries/marine
	(:refer-clojure)
  (:refer modelling :only (defmodel measurement classification categorization ranking identification bayesian)))

;; --------------------------------------------------------------------------------------
;; sink (coastal protection) model
;; --------------------------------------------------------------------------------------

;; TODO almost all coral polygons in existing data do not report bleaching; I 
;; don't know what text should be in the categories to define other states, so
;; only HighBleaching is reported here if the field isn't empty.
(defmodel bleaching 'coastalProtection:CoralBleaching
	(classification (categorization 'coastalProtection:CoralBleaching)
 		#{nil "None"}			  'coastalProtection:NoBleaching
 		#{"HIgh" "High"}	  'coastalProtection:HighBleaching
 	  #{"Low" "Moderate"} 'coastalProtection:ModerateBleaching))
	
;; TODO only two classes represented from presence/absence; no idea how to 
;; model density based on existing data.
(defmodel seagrass 'coastalProtection:SeagrassDensity
	(classification (ranking 'coastalProtection:SeagrassDensity)
		0 'coastalProtection:NoSeagrassDensity
		1 'coastalProtection:HighSeagrassDensity))
		
;; sea floor slope 
;; Ken: units uncertain, layer is horribly large and cumbersome to work with
;; TODO check - BN has only 4 classes, so I put the last 2 together and eliminated
;; VeryHighSlope with breakpoint at 9,000,000
(defmodel slope 'coastalProtection:BathymetricSlope
	(classification (ranking 'habitat:BathymetricSlope)
		[4000000 :>]      'coastalProtection:HighSlope
		[2000000 4000000] 'coastalProtection:ModerateSlope
		[600000 4000000]  'coastalProtection:LowSlope
		[:< 2000000]      'coastalProtection:VeryLowSlope))
			
;; flood protection
(defmodel coastal-flood-sink 'coastalProtection:CoastalFloodSink
  	"Interface to Flood public asset use bayesian network"
	  (bayesian 'coastalProtection:CoastalFloodSink)
	  	:import   "aries.marine::CoastalFloodSink.xdsl"
	  	:keep     ('coastalProtection:TotalCoastalFloodProtection)
	 	 	:context  (bleaching seagrass slope))

;; --------------------------------------------------------------------------------------
;; use models
;; --------------------------------------------------------------------------------------

(defmodel risk-to-life 'coastalProtection:CycloneDependentLivesAtRisk
	(classification (ranking 'policytarget:LivesAtRiskStorm)
		[8 :>]   'coastalProtection:HighRiskToLife
		[5 8]    'coastalProtection:ModerateRiskToLife
		[1 5]    'coastalProtection:LowRiskToLife
		[:< 1]   'coastalProtection:NoRiskToLife))

(defmodel risk-to-assets 'coastalProtection:CycloneSensitiveEconomicValue
	(classification (ranking 'policytarget:AssetsAtRiskStorm)
		[8 :>]   'coastalProtection:HighRiskToAssets
		[5 8]    'coastalProtection:ModerateRiskToAssets
		[1 5]    'coastalProtection:LowRiskToAssets
		[:< 1]   'coastalProtection:NoRiskToAssets))
		
;; --------------------------------------------------------------------------------------
;; source models
;; --------------------------------------------------------------------------------------

(defmodel storm-probability 'coastalProtection:TropicalStormProbability
	(ranking 'habitat:TropicalStormProbability))

;; --------------------------------------------------------------------------------------
;; all together now
;; --------------------------------------------------------------------------------------

(defmodel coastal-protection-data 'coastalProtection:CoastalStormProtection
	(identification 'coastalProtection:CoastalStormProtection)
		:context (coastal-flood-sink risk-to-life risk-to-assets storm-probability))

	 	 	