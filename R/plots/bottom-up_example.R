# 
# This script plots the different distributions .
#
startTimeAll <- proc.time()



## Libraries

library(igraph)
library(ggplot2)



## Parameters

targets <- c("P55087")

mainCategoryNames <- c("Complexes", "Pathways", "Interactions", "Composite")
categoryNames <- c("Complexes", "Reactome", "Kegg", "Biogrid", "Intact", "String")
categoryColors <- c("#e31a1c", "#1f78b4", "#33a02c", "#ff7f00", "#6a3d9a", "#999999")



## Functions

#' Returns the edges containing the targets.
#' 
#' @param edges the edges data frame
#' @param targets the targets
getEdges <- function(edges, targets) {
  
  firstDegree <- edges[edges$from %in% targets | edges$to %in% targets, ]
  secondDegree <- edges[edges$from %in% firstDegree$from 
                        & edges$to %in% firstDegree$to
                        & !edges$from %in% targets
                        & !edges$to %in% targets, ]
  
  edgesTargets <- rbind(firstDegree, secondDegree)
  
  return(edgesTargets)
}

#' Returns the vertices containing the targets.
#' 
#' @param vertices the vertices data frame
#' @param edgestargets the edges targets data frame
#' @param targets the targets
getVertices <- function(vertices, edgestargets, targets) {
  
  verticestargets <- vertices[vertices$id %in% edgestargets$from | vertices$id %in% edgestargets$to, ]
  
  if (nrow(verticestargets) == 0) {
    
    verticestargets[1, ] <- c("target", "target")
    
  } else {
    
    verticestargets$name[verticestargets$id == targets] <- "target"
    
  }
  
  return(verticestargets)
}



## Main script


# Load data

print(paste(Sys.time(), " Loading data", sep = ""))

verticesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesComplexes <- read.table("resources/iGraph/complexes/complexes_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesReactome <- read.table("resources/iGraph/reactome/reactome_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesKegg <- read.table("resources/iGraph/kegg/kegg_21.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesKegg <- read.table("resources/iGraph/kegg/kegg_21.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesBiogrid <- read.table("resources/iGraph/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesBiogrid <- read.table("resources/iGraph/biogrid/BIOGRID-ORGANISM-Homo_sapiens-3.4.151_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesIntact <- read.table("resources/iGraph/intact/intact_18.08.17_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")

verticesString <- read.table("resources/iGraph/string/string_v10.5_medium_vertices", header = T, sep = "\t", stringsAsFactors = F, quote = "", comment.char = "")
edgesString <- read.table("resources/iGraph/string/string_v10.5_medium_edges", header = T, sep = " ", stringsAsFactors = F, quote = "", comment.char = "")


# Select direct connections to targets

print(paste(Sys.time(), " Finding targets", sep = ""))

edgesComplexestargets <- getEdges(edges = edgesComplexes, targets = targets)
verticesComplexestargets <- getVertices(vertices = verticesComplexes, edgestargets = edgesComplexestargets, targets = targets)

edgesReactometargets <- getEdges(edges = edgesReactome, targets = targets)
verticesReactometargets <- getVertices(vertices = verticesReactome, edgestargets = edgesReactometargets, targets = targets)

edgesKeggtargets <- getEdges(edges = edgesKegg, targets = targets)
verticesKeggtargets <- getVertices(vertices = verticesKegg, edgestargets = edgesKeggtargets, targets = targets)

edgesBiogridtargets <- getEdges(edges = edgesBiogrid, targets = targets)
verticesBiogridtargets <- getVertices(vertices = verticesBiogrid, edgestargets = edgesBiogridtargets, targets = targets)

edgesIntacttargets <- getEdges(edges = edgesIntact, targets = targets)
verticesIntacttargets <- getVertices(vertices = verticesIntact, edgestargets = edgesIntacttargets, targets = targets)

edgesStringtargets <- getEdges(edges = edgesString, targets = targets)
verticesStringtargets <- getVertices(vertices = verticesString, edgestargets = edgesStringtargets, targets = targets)


# Make the networks

print(paste(Sys.time(), " Making networks", sep = ""))

complexes <- graph_from_data_frame(d = edgesComplexestargets, vertices = verticesComplexestargets, directed = F)
reactome <- graph_from_data_frame(d=edgesReactometargets, vertices=verticesReactometargets, directed=T)
kegg <- graph_from_data_frame(d = edgesKeggtargets, vertices = verticesKeggtargets, directed = F)
biogrid <- graph_from_data_frame(d = edgesBiogridtargets, vertices = verticesBiogridtargets, directed = F)
intact <- graph_from_data_frame(d = edgesIntacttargets, vertices = verticesIntacttargets, directed = F)
string <- graph_from_data_frame(d = edgesStringtargets, vertices = verticesStringtargets, directed = F)


# Plot the networks

print(paste(Sys.time(), " Plotting networks", sep = ""))

png("resources/iGraph/plots/targeted/complexes.png", width = 800, height = 600)
plot(complexes, vertex.shape = "none", edge.color = categoryColors[1])
dummy <- dev.off()

png("resources/iGraph/plots/targeted/reactome.png", width = 800, height = 600)
plot(reactome, vertex.shape = "none", edge.color = categoryColors[1])
dummy <- dev.off()

png("resources/iGraph/plots/targeted/kegg.png", width = 800, height = 600)
plot(kegg, vertex.shape = "none", edge.color = categoryColors[1])
dummy <- dev.off()

png("resources/iGraph/plots/targeted/biogrid.png", width = 800, height = 600)
plot(biogrid, vertex.shape = "none", edge.color = categoryColors[1])
dummy <- dev.off()

png("resources/iGraph/plots/targeted/intact.png", width = 800, height = 600)
plot(intact, vertex.shape = "none", edge.color = categoryColors[1])
dummy <- dev.off()

png("resources/iGraph/plots/targeted/string.png", width = 800, height = 600)
plot(string, vertex.shape = "none", edge.color = categoryColors[1])
dummy <- dev.off()


