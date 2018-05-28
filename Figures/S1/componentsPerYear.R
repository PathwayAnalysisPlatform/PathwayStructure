path <- "../../resources/centralities/"

allFiles <- list.files(path=path,
                       pattern="nShortestPaths.csv",
                       full.names=TRUE,
                       recursive=FALSE)
length(allFiles)
allFiles <-
    allFiles[grepl(paste0(path, "[^[:alpha:]]"), allFiles)]
length(allFiles)
nrowPrev <- 0
allMats <- list()
for (thisFile in allFiles) {
    pathName <- sub("nShortestPaths.csv", "",
                    sub(path, "",
                        thisFile))
    adjMat <- as.matrix(read.csv(thisFile,
                                 row.names=1))
    if (nrow(adjMat) > nrowPrev) {
        nrowPrev <- nrow(adjMat)
        allMats[[pathName]] <- adjMat
    }
}

doBFSthing <- function (theMat) {
    ##CofN Component of the Node
    CofN <- rep(NA, nrow(theMat))
    print(paste("size of matrix", nrow(theMat)))
    component <- 0
    while (any(is.na(CofN))) {
        component <- component + 1
        ## which nodes are in this component?
        inThisComponent <- min(which(is.na(CofN)))
        ## counter for inThisComponent
        iTCidx <- 0
        while (iTCidx < length(inThisComponent)) {
            iTCidx <- iTCidx + 1
            ## print(paste(iTCidx, "<", length(inThisComponent)))
            ## print(inThisComponent[iTCidx])
            ## add the current node to this component
            CofN[inThisComponent[iTCidx]] <- component
            ## which nodes can the current node reach?
            canReach <- c(which(theMat[inThisComponent[iTCidx], ] > 0),
                          which(theMat[, inThisComponent[iTCidx]] > 0))
            ## add those to the end of the set of nodes to visit
            inThisComponent <-
                c(inThisComponent,
                  setdiff(canReach, inThisComponent))
        }
    }
    CofN
}

components <- list()
for (i in seq_along(allMats)) {
    print(names(allMats)[i])
    components[[names(allMats)[i]]] <-
        doBFSthing(allMats[[i]])
}

library("ggplot2")
figs2 <- ggplot(data.frame(year = as.numeric(names(components)),
                           clust = sapply(components, max)),
                aes(year, clust)) +
    geom_point(col=rgb(137, 208, 245, maxColorValue=250)) +
    geom_line(col=rgb(137, 208, 245, maxColorValue=250)) +
    scale_x_continuous(name = "Year") +
    scale_y_continuous(name = "# Connected clusters")

ggsave("FigureS1.pdf", figs2, width=174, height=100, units="mm")
