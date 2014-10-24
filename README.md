##Invariantes da lista ligada:

*(..., a, b, c, ...)* **=>** Secção da lista em que **a** referencia **b**, que referencia **c**.

* No final da remoção de **b**, **a** deve apontar para **c**.
* No final da inserção de **x** na posição de **b**, **a** aponta para **x**, **x** aponta para **b**, **b** aponta para **c**.
* Durante a leitura da lista, nenhum nó (que não o último) deve referenciar **null**.