paths <-
    as.matrix(
        read.csv(
            "../../Java/PathwayQuery/7.20.17 no harmonic/lengthShortestPaths.csv",
            row.names=1))
oz <- c(read.table("olfact.list", stringsAsFactors=FALSE)$V1,
        read.table("zinc.list", stringsAsFactors=FALSE)$V1)
paths <- paths[-which(rownames(paths) %in% oz),
               -which(rownames(paths) %in% oz)]
paths <- paths == 1
degrees <- rowSums(paths)


kI <- sort(unique(degrees))
kN <- length(degrees)
kF <- rep(NA, length(kI))
for (i in seq_len(length(kI))) {
    kF[i] <- sum(degrees == kI[i])/kN
}
plot(kI, kF)

head(kI[order(kF, decreasing=TRUE)])
head(kF[order(kF, decreasing=TRUE)])

pOut <-
    as.matrix(
        read.csv(
            "../../Java/PathwayQuery/7.20.17 no harmonic/OutlengthShortestPaths.csv",
            row.names=1))
oz <- c(read.table("olfact.list", stringsAsFactors=FALSE)$V1,
        read.table("zinc.list", stringsAsFactors=FALSE)$V1)
pOut <- paths[-which(rownames(pOut) %in% oz),
              -which(rownames(pOut) %in% oz)]
pOut <- pOut == 1
dOut <- rowSums(pOut)

kOutI <- sort(unique(dOut))
kOutN <- length(dOut)
kOutF <- rep(NA, length(kOutI))
for (i in seq_len(length(kOutI))) {
    kOutF[i] <- sum(dOut == kOutI[i])/kOutN
}
plot(kOutI, kOutF)

head(kOutI[order(kOutF, decreasing=TRUE)])
head(kOutF[order(kOutF, decreasing=TRUE)])

yticks <- c("0.0001", "0.0005", "0.001",
            "0.005", "0.01", "0.05", "0.1")
xticks <- c("1", "5", "10", "50",
            "100", "500", "1000")
plot(kOutI, kOutF, log='xy', col='magenta',
     main = "Node Out-Degree Distribution",
     ylab = "p(k)", xlab = "k",
     ylim = c(0.0001, 0.1),
     xlim = c(1, 1000),
     pch = 2,
     axes = FALSE)
axis(2, at=as.numeric(yticks), labels=yticks, las=1)
axis(1, at=as.numeric(xticks), labels=xticks)


pIn <-
    as.matrix(
        read.csv(
            "../../Java/PathwayQuery/7.20.17 no harmonic/InlengthShortestPaths.csv",
            row.names=1))
oz <- c(read.table("olfact.list", stringsAsFactors=FALSE)$V1,
        read.table("zinc.list", stringsAsFactors=FALSE)$V1)
pIn <- paths[-which(rownames(pIn) %in% oz),
              -which(rownames(pIn) %in% oz)]
pIn <- pIn == 1
dIn <- rowSums(pIn)

kInI <- sort(unique(dIn))
kInN <- length(dIn)
kInF <- rep(NA, length(kInI))
for (i in seq_len(length(kInI))) {
    kInF[i] <- sum(dIn == kInI[i])/kInN
}
plot(kInI, kInF)

pdf("InOutDegreeHy.pdf", width=14, height=7)
par(mfrow=c(1,2))
yticks <- c("0.0001", "0.0005", "0.001",
            "0.005", "0.01", "0.05", "0.1")
xticks <- c("1", "5", "10", "50",
            "100", "500", "1000")
plot(kInI, kInF, log='xy',
     main = "Node In-Degree Distribution",
     ylab = "p(k)", xlab = "k",
     ylim = c(0.0001, 0.1),
     xlim = c(1, 1000),
     pch = 2,
     axes = FALSE)
axis(2, at=as.numeric(yticks), labels=yticks, las=1)
axis(1, at=as.numeric(xticks), labels=xticks)
plot(kOutI, kOutF, log='xy', col='magenta',
     main = "Node Out-Degree Distribution",
     ylab = "p(k)", xlab = "k",
     ylim = c(0.0001, 0.1),
     xlim = c(1, 1000),
     pch = 2,
     axes = FALSE)
axis(2, at=as.numeric(yticks), labels=yticks, las=1)
axis(1, at=as.numeric(xticks), labels=xticks)
dev.off()
