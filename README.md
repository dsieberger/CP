##Estratégias de consistência na manipulação de dados:

O programa contém uma arquitectura de dados especificada por **um repositório** que contém **3 *Hashmaps***:

1. Os artigos de um autor => *Map(String, List&lt;Article&gt;)*
2. As *keywords* de um artigo => *Map(String, List&lt;Article&gt;)*
3. O *ID* de um artigo => *Map(Integer, Article)*

As duas primeiras estruturas mencionadas têm como valor uma **lista ligada** que contém os elementos referidos.

* ###*Fine-grained synchronization*:
 * Aplicar *locks* apenas ao mais baixo nível possível.
 * As estruturas mais elementares, são os próprios nós dessas listas ligadas e os artigos no 3º mapa.
 * Como os nós da lista são referenciados por outros nós, para manter consistência usa-se a técnica ***Hand-over-hand locking***.
 * Para os próprios artigos poder-se-á apenas garantir exclusão mútua na manipulação do objecto.

* ###Atomicidade:
 * Como usamos ***hand-over-hand locking*** sabemos que qualquer processo que itere a lista nunca ultrapassará outro processo que já o esteja a fazer.
 * Portanto se um processo inserir um elemento na lista, então um outro processo que queira remover esse elemento e que seja invocado depois, garantidamente que não o fará antes do primeiro processo terminar.
 * Porém há o problema de desaparecer a atomicidade quando se salta de uma lista para outra (para remover um artigo de um conjunto de autores, por exemplo)
 * Se extendermos este ***hand-over-hand locking*** para a manipulação de várias listas de um mapa, manter-se-ia a consistência das operações de adicionar um artigo aos vários autores, ou uma *keyword* a cada artigo, bem como a remoção dos mesmos.
 * Assuma-se que se quer inserir um artigo a um conjunto de autores. Ordena-se este conjunto pelos seus *hashcodes* (por exemplo) antes de cada operação de escrita para controlar um acesso ordenado aos dados.
 * Assim, sabemos que a ordem de acesso aos autores (neste caso) é global entre todos os processos  e garante-se atomicidade se no final de uma iteração de uma lista, só se fizer *unlock* do nó dessa lista depois do *lock* do início da próxima lista a iterar:
 * [ autor 1 ] || [ artigo 1 ] -> [ artigo 2 ] -> [ **artigo 3** ] -> (**garantir atomicidade entre a lista do *autor 1* e a do *autor 2***)
 * [ autor 2 ] || [ artigo 6 ] -> [ **artigo 3** ] -> [ artigo 5 ]

* ###*Lock ordering*:
 * Uma operação de inserção ou remoção do repositório, deve garantir que o facto de se lidarem com estruturas distintas (*Hashmaps* distintos) não compromete a consistência dos dados.
 * Se um processo **p1** inserir um novo artigo e um outro processo **p2** quiser removê-lo, pode surgir uma situação em que **p1** já inseriu os dados na 1ª e na 2ª tabelas de dispersão mas ainda não na 3ª. Depois **p2**, elimina os dados inseridos por **p1** e termina a execução porque não encontrou o conteúdo na 3ª tabela. Finalmente, **p1** prossegue a sua execução e termina-a ao adicionar os dados em falta na última estrutura. O programa termina com um estado não consistente.
 * Se a escrita no repositório for uma sequência ordenada sempre da mesma forma em relação às estruturas de dados que modifica, podemos garantir consistência.

##Invariantes da lista ligada:

*(..., a, b, c, ...)* **=>** Secção da lista em que **a** referencia **b**, que referencia **c**.

* Uma lista vazia consiste apenas de um elemento denominado **head** que não tem qualquer significado no contexto externo a esta estrutura.
* No final da remoção de **b**, **a** deve apontar para **c**.
* No final da inserção de **x** na posição de **b**, **a** aponta para **x**, **x** aponta para **b**, **b** aponta para **c**.
* Durante a leitura da lista, nenhum nó (que não o último) deve referenciar **null**.