##########################################################
#Author: Marije van der Geest & Erik Schutte             #
#                                                        #
# This data is filtered for Norway data and NA's.        #
#                                                        #
#Generates line plot for gluten specific T-cell          #
#expression per gene under different conditions          #
#(timpoints 0, 10, 30, 180)                              #
##########################################################

########
# Libs #
########
library(grDevices)


#Create connection with molgenis
source("http://localhost:8080/molgenis.R")
molgenis.login("admin", "admin")

#Set time points
time.points <- c("_0$", "_10$", "_30$", "_180$")
time.labels <- c(0,10,30,180)

#Get and process genes (input)
genes <- "ENSG00000115598,ENSG00000115602,"
genes <- unlist(strsplit(genes, '[,]'))


#Create query for filtering data
qs <- ""
for (i in 1:length(genes)){
  qs[i] <- paste("EnsemblGeneID==", genes[i], sep="")
}
q=paste(qs, collapse=",")
gs.tcell <- molgenis.get("lncrna_data_Tcell123samples", q=q)

#Format data
rownames(gs.tcell) <- gs.tcell[,1]
gs.tcell <- gs.tcell[,-1]


#Generate image
colors <- rainbow(length(genes))
png("${outputFile}")

#Create empty plot
plot(0,type="b", col="red", xlab="Time [min]", ylab="Mean expression", xaxt="n", ylim=c(0,10), xlim=c(1,4))
axis(1, at=1:length(time.labels), labels=as.vector(time.labels))

#Draw lines for each gene
for (i in 1:length(genes)){
  exp.values <- gs.tcell[genes[i],]
  meanExp.perTimepoint <- lapply(time.labels, function(x) mean(as.numeric(exp.values[grep(x, names(exp.values), value=T)])))
  meanExp.perTimepoint.matrix <- t(matrix(unlist(meanExp.perTimepoint)))
  colnames(meanExp.perTimepoint.matrix) <- c(0,10,30,180)
  lines(t(meanExp.perTimepoint.matrix), type="b", col=colors[i], lwd=2, pch=16)
  
}


#Create query for gene id -> gene name conversion
qs <- ""
for (i in 1:length(genes)){
  qs[i] <- paste("EnsemblGeneID==", genes[i], sep="")
}
q=paste(qs, collapse=",")
#Convert genes
conversion.table <- molgenis.get("lncrna_GeneInfo", q=q)
genes = as.character(conversion.table[do.call(c,lapply(genes, function(x) which(as.character(conversion.table[,1]) == x))),2])

#Generate legend
legend(x="topright", legend=genes, col=colors, pch=19, cex=0.8)

