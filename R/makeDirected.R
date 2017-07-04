source("directedFunctions.R")

path <- "../Java/PathwayQuery/"
inFile <- paste0(path, "ProteomeGraph.sif")

edgeTypes <- c("io", "ro", "co")
outFile <- paste0("All", "InCatRegToOutput.sif")
outFile <- paste0(path, outFile)
makeDirected(inFile, outFile, edgeTypes)

edgeTypes <- c("oi", "or", "oc")
outFile <- paste0("All", "OutputToInCatReg.sif")
outFile <- paste0(path, outFile)
makeDirected(inFile, outFile, edgeTypes)
