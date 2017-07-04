source("makeDirected")

inFile <- "../Java/PathwayQuery/ProteomeGraph.sif"
edgeTypes <- c("io", "ro", "co")
outFile <- paste0("All", "InCatRegToOutput.sif")
makeDirected(inFile, outFile, edgeTypes)

edgeTypes <- c("oi", "or", "oc")
outFile <- paste0("All", "OutputToInCatReg.sif")
makeDirected(inFile, outFile, edgeTypes)
