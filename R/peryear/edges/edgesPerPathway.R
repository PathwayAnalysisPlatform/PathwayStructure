### Get the proteins per pathway
pathPathway <- "../../../Java/PathwayQuery/perPathway170808/"

allFiles <- list.files(path=pathPathway,
                       pattern="centralities.csv",
                       full.names=TRUE,
                       recursive=FALSE)
length(allFiles)
protPerPath <- list()
for (thisFile in allFiles) {
    pathName <- sub("centralities.csv", "",
                    sub(pathPathway, "",
                        thisFile))
    if (nchar(pathName) == 0) {
        pathName <- "All"
    }
    print(pathName)
    protPerPath[[pathName]] <-
        read.csv(thisFile, stringsAsFactors=FALSE)$Protein
}



### get the edges per year
## for each year
## edges => three columns : from, to, type
## {
##     cat("   ")
##     years <- 1934:2017
##     names(years) <- years
##     vertices <- lapply(years, function (year) {
##         cat(paste0("\b\b\b\b", year))
##         adjList <- list()
##         ## will be adjList[[typeConnection]][[startNode]]
##         inFile <- paste0("../../../Java/PathwayQuery/", year, ".sif")
##         adjList <- tryCatch({
##             con <- file(inFile, "r")
##             while ( TRUE ) {
##                 line <- readLines(con, n = 1)
##                 if ( length(line) == 0 ) {
##                     break
##                 }
##                 theLine <- strsplit(line, " ")[[1]]
##                 theNode <- theLine[1]
##                 typeConn <- theLine[2]
##                 adjList[[typeConn]][[theNode]] <- list()
##                 adjList[[typeConn]][[theNode]] <-
##                     theLine[3:length(theLine)]
##             }
##             adjList },
##             finally={
##                 close(con)
##             })
##         Ntype <- unlist(sapply(names(adjList), function (x) {
##             rep(x, sum(sapply(adjList[[x]], length))) }))
##         fNode <- unlist(sapply(names(adjList), function (x) {
##             unlist(sapply(names(adjList[[x]]), function (y) {
##                 rep(y, length(adjList[[x]][[y]])) })) }))
##         tNode <- unlist(adjList)
##         vert <- data.frame(from = fNode,
##                            to = tNode,
##                            type = Ntype,
##                            stringsAsFactors=FALSE)
##         vert
##     })
##     cat("\n")
## }
## names(vertices)

## for (year in names(vertices)) {
##     colnames(vertices[[year]]) <- c("from", "to", "type")
##     write.csv(vertices[[year]],
##               file = paste0(year, "edgesPerYear.csv"),
##               row.names=FALSE)
## }

?duplicated
duplicated(matrix(c(1,2,1,2,1,3), nrow=3, byrow=TRUE))

years <- 1934:2017
names(years) <- years
vertices <- lapply(years, function (year) {
    as.matrix(read.csv(paste0(year, "edgesPerYear.csv")))
})

thething <-
    data.frame(edges = sapply(vertices, nrow),
               nodes = sapply(vertices, function (x)
                   length(union(x[, "from"], x[, "to"]))))

## protPerPath[[pathway]]
## vertices[[year]]
## head(vertices[["1934"]])

nrEdgesPathwayYear <-
    lapply(names(protPerPath), function (pathway) {
        sapply(as.character(1934:2017), function (year) {
            sum(!duplicated(vertices[[year]][
                     vertices[[year]][,"from"] %in%
                     protPerPath[[pathway]] &
                     vertices[[year]][,"to"] %in%
                     protPerPath[[pathway]],
                     c("from", "to")]))
        })
    })
names(nrEdgesPathwayYear) <- names(protPerPath)
totalEdges <- max(unlist(nrEdgesPathwayYear))

nrEdgesPathwayYearNA <- nrEdgesPathwayYear[-4]
totalEdgesNA <- max(unlist(nrEdgesPathwayYearNA))

pathwayOccurrence <-
    names(nrEdgesPathwayYear)[
        order(sapply(nrEdgesPathwayYear, function (x) {
            min(which(x > 0)) }))]
pathwayOccurrenceNA <- pathwayOccurrence[-1]

## Plot number of all edges per year per pathway
## max one edge from A to B
pdf("edgesPathwayYear1960.pdf", width=25, height=15)
par(mfrow=c(5,5))
for (pathway in names(protPerPath)[pathwayOccurrence]) {
    plot(seq(1960, 2017),
         nrEdgesPathwayYear[[pathway]][seq(1960, 2017)-1934],
         type='l',
         xlim=c(1960, 2020),
         xlab="Year", ylab="# Edges",
         main=pathway)
}
dev.off()

pdf("notInformative.pdf", width=25, height=15)
par(mfrow=c(5,5))
for (pathway in names(protPerPath)[pathwayOccurrence]) {
    plot(seq(1960, 2017),
         nrEdgesPathwayYear[[pathway]][seq(1960, 2017)-1934],
         type='l',
         xlim=c(1960, 2020),
         ylim=c(0,totalEdges),
         xlab="Year", ylab="# Edges",
         main=pathway)
}
dev.off()

logedges <- lapply(nrEdgesPathwayYearNA, function (x) {
    ifelse(x > 0, log(x), NA)
})
names(logedges) <- names(nrEdgesPathwayYearNA)

par(mfrow=c(4,6))
for (pathway in names(protPerPath)[pathwayOccurrence]) {
    plot(seq(1960, 2017),
         logedges[[pathway]][seq(1960, 2017)-1934],
         type='l',
         xlim=c(1960, 2020),
         ylim=c(0,max(unlist(logedges), na.rm=TRUE)),
         xlab="Year", ylab="# Edges",
         main=pathway)
}

par(mfrow=c(1,1))
myrepcol <- rep(rainbow(5), each=5)
pdf("togetherlogedgesperyearperpathway.pdf", width=14, height=10)
plot(NULL,
     xlim=c(1960, 2020),
     ylim=c(0,max(unlist(logedges), na.rm=TRUE)),
     xlab="Year", ylab="log of # Edges",
     main="Pathway")
i <- 0
for (pathway in pathwayOccurrenceNA) {
    i <- i+1
    lines(seq(1960, 2017),
          logedges[[pathway]][seq(1960, 2017)-1934],
          col=myrepcol[i])
}
dev.off()

par(mfrow=c(1,1))
pdf("edgesPathwayYearFractionOnePlotBW.pdf", width=14, height=10)
plot(NULL,
     xlim=c(1960, 2020),
     ylim=c(0,1),
     xlab="Year", ylab="# Edges",
     main="")
for (pathway in names(protPerPath)[pathwayOccurrence]) {
    lines(seq(1960, 2017),
          nrEdgesPathwayYear[[pathway]][seq(1960, 2017)-1934] /
          nrEdgesPathwayYear[[pathway]][2017-1934])
}
dev.off()

par(mfrow=c(5,5))
for (pathway in names(protPerPath)) {
    plot(seq(1960, 2017),
         sapply(as.character(1960:2017), function (year) {
             sum(!duplicated(vertices[[year]][
                      vertices[[year]][,"from"] %in%
                      protPerPath[[pathway]] &
                      vertices[[year]][,"to"] %in%
                      protPerPath[[pathway]],
                      c("from", "to")]))
         }), type='l',
         xlim=c(1960, 2020),
         xlab="Year", ylab="# Edges",
         main=pathway)
}

## on one page
pdf("edgesPathwayYear.pdf", width=25, height=15)
par(mfrow=c(5,5))
for (pathway in names(protPerPath)) {
    plot(seq(1934, 2017),
         sapply(as.character(1934:2017), function (year) {
             sum(!duplicated(vertices[[year]][
                      vertices[[year]][,"from"] %in%
                      protPerPath[[pathway]] &
                      vertices[[year]][,"to"] %in%
                      protPerPath[[pathway]],
                      c("from", "to")]))
         }), type='l',
         xlim=c(1930, 2020),
         xlab="Year", ylab="# Edges",
         main=pathway)
}
dev.off()

## on separate pages
pdf("edgesPathwayYearSeparate.pdf", width=14, height=10)
for (pathway in names(protPerPath)) {
    plot(seq(1934, 2017),
         sapply(as.character(1934:2017), function (year) {
             sum(!duplicated(vertices[[year]][
                      vertices[[year]][,"from"] %in%
                      protPerPath[[pathway]] &
                      vertices[[year]][,"to"] %in%
                      protPerPath[[pathway]],
                      c("from", "to")]))
         }), type='l',
         xlim=c(1930, 2020),
         xlab="Year", ylab="# Edges",
         main=pathway)
}
dev.off()


## Plot fraction of all edges per year per pathway per type
## Reference is total number of edges in 2017
## (different types of edges from A to B are counted as different connections)
par(mfrow=c(5,5))
for (pathway in names(protPerPath)) {
    plot(NULL,
         xlim=c(1930, 2020), ylim=c(0,1),
         xlab="Year", ylab="# Edges",
         main=pathway)
    edgetype <- c("io", "co", "ro")
    edgecol <- c("black", "blue", "red")
    theLines <-
        lapply (seq_along(edgetype), function (i) {
            sapply(as.character(1934:2017), function (year) {
                nrow(vertices[[year]][
                    vertices[[year]][,"from"] %in%
                    protPerPath[[pathway]] &
                    vertices[[year]][,"to"] %in%
                    protPerPath[[pathway]] &
                    vertices[[year]][,"type"] ==
                    edgetype[i], , drop = FALSE]) }) })
    nEdges <- sum(sapply(theLines, function (x) x[length(x)]))
    for (i in seq_along(edgetype)) {
        lines(seq(1934, 2017),
              theLines[[i]]/nEdges,
              col=edgecol[i])
    }
}

## on one page
pdf("edgesPathwayYearType.pdf", width=25, height=15)
par(mfrow=c(5,5))
for (pathway in names(protPerPath)) {
    plot(NULL,
         xlim=c(1930, 2020), ylim=c(0,1),
         xlab="Year", ylab="# Edges",
         main=pathway)
    edgetype <- c("io", "co", "ro")
    edgecol <- c("black", "blue", "red")
    theLines <-
        lapply (seq_along(edgetype), function (i) {
            sapply(as.character(1934:2017), function (year) {
                nrow(vertices[[year]][
                    vertices[[year]][,"from"] %in%
                    protPerPath[[pathway]] &
                    vertices[[year]][,"to"] %in%
                    protPerPath[[pathway]] &
                    vertices[[year]][,"type"] ==
                    edgetype[i], , drop = FALSE]) }) })
    nEdges <- sum(sapply(theLines, function (x) x[length(x)]))
    for (i in seq_along(edgetype)) {
        lines(seq(1934, 2017),
              theLines[[i]]/nEdges,
              col=edgecol[i])
    }
}
dev.off()

## on separate pages
pdf("edgesPathwayYearTypeSeparate.pdf", width=14, height=10)
for (pathway in names(protPerPath)) {
    plot(NULL,
         xlim=c(1930, 2020), ylim=c(0,1),
         xlab="Year", ylab="# Edges",
         main=pathway)
    edgetype <- c("io", "co", "ro")
    edgecol <- c("black", "blue", "red")
    theLines <-
        lapply (seq_along(edgetype), function (i) {
            sapply(as.character(1934:2017), function (year) {
                nrow(vertices[[year]][
                    vertices[[year]][,"from"] %in%
                    protPerPath[[pathway]] &
                    vertices[[year]][,"to"] %in%
                    protPerPath[[pathway]] &
                    vertices[[year]][,"type"] ==
                    edgetype[i], , drop = FALSE]) }) })
    nEdges <- sum(sapply(theLines, function (x) x[length(x)]))
    for (i in seq_along(edgetype)) {
        lines(seq(1934, 2017),
              theLines[[i]]/nEdges,
              col=edgecol[i])
    }
    legend('topleft',
           legend=c("input - output",
                    "catalyst - output",
                    "regulator - output"),
           col=edgecol, lty=1)
}
dev.off()
