##########################################################
#Author: Marije van der Geest & Erik Schutte             #
#                                                        #
#Generates line plot for gluten specific T-cell          #
#expression per gene under different conditions          #
#(timpoints 0, 10, 30, 180)                              #
##########################################################

########
# Libs #
########
library(gplots)

#Make connection
source("http://localhost:8080/molgenis.R")
molgenis.login("admin", "admin")

#Get and process genes (input)
genes <- "ENSG00000131748,ENSG00000173991,ENSG00000141744,ENSG00000161395,ENSG00000141736,ENSG00000265178,ENSG00000264198,ENSG00000264663,ENSG00000226117,ENSG00000264968,ENSG00000265799,ENSG00000238793,ENSG00000221555,ENSG00000141741,ENSG00000141738,ENSG00000161405,ENSG00000186075,ENSG00000073605,ENSG00000172057,ENSG00000204913,ENSG00000167914,ENSG00000108344,ENSG00000108342,ENSG00000008838,ENSG00000126351,ENSG00000126368,ENSG00000188895,ENSG00000108349,"
genes <- unlist(strsplit(genes, '[,]'))

#Create query for filtering data
qs <- ""
for (i in 1:length(genes)){
  qs[i] <- paste("EnsemblGeneID==", genes[i], sep="")
}
q=paste(qs, collapse=",")

#Get data
LLDeep <- molgenis.get("lncrna_data_Tcell123samples", num=100000, q=q)
rownames(LLDeep) <- LLDeep[,1]
LLDeep <- LLDeep[,-1]
genes.length <- nrow(LLDeep)


#Get query for gene id -> gene name conversion
qs <- ""
for (i in 1:length(genes)){
  qs[i] <- paste("EnsemblGeneID==", genes[i], sep="")
}
q=paste(qs, collapse=",")

#Convert genes
conversion.table <- molgenis.get("lncrna_GeneInfo", q=q)
#row.names(LLDeep) = as.character(conversion.table[do.call(c,lapply(row.names(LLDeep), function(x) which(as.character(conversion.table[,1]) == x))),2])

#Perform correlation (Spearman)
correlation.matrix <- cor(t(LLDeep), method="spearman", use="pairwise.complete.obs")

#Create image
#png("${outputFile}")
colors <- colorRampPalette(c("gold", "midnightblue", 'blue'))(10)
heatmap.2(correlation.matrix, trace="none", 
          colsep=c(1:genes.length), rowsep=c(1:genes.length), 
          sepcolor="snow2", sepwidth=c(0.05,0.05), col=colors,
          cexRow=0.5, cexCol=0.5, key.par=list(c(-1, 0, 0.5, 1)), margin=c(7,10),
          labRow=conversion.table[,2],labCol=conversion.table[,2])
