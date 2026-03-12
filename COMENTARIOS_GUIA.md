# GUIA COMENTADO - THE LAST ROAR

Este arquivo descreve todos os comentários adicionados ao projeto para facilitar a modificação do código.

## ESTRUTURA DO PROJETO

```
TheLastRoar/
├── App.java              → Classe principal (tela do mapa, movimento, IA)
├── Character.java        → Personagem do jogador
├── Battle.java           → Sistema de batalha
├── Monsters.java         → Classe base de inimigos
├── Goblin.java           → Inimigo tipo Goblin
├── Sword.java            → Sistema de armas
├── Armor.java            → Sistema de armaduras
├── Potion.java           → Poções de cura
├── Item.java             → Classe base para itens
├── Inventory.java        → Sistema de mochila
└── StartScreen.java      → Tela inicial
```

---

## ARQUIVOS COMENTADOS DETALHADAMENTE

### 1. App.java (CLASSE PRINCIPAL DO JOGO)

**O que faz:**
- Carrega o mapa do jogo
- Controla movimento do jogador com teclado (W/A/S/D ou setas)
- Anima sprite do jogador em 4 direções
- Controla IA do inimigo (movimento automático)
- Detecta colisão entre jogador e inimigo
- Inicia batalha quando colidem

**Variáveis importantes:**
- `speed = 3` → Velocidade do movimento (pixels por frame)
- `frameDelay = 200_000_000` → Tempo entre frames de animação (em nanosegundos)
- `direction = 0` → Direção atual (0=baixo, 1=esquerda, 2=direita, 3=cima)

**Modificar movimento:**
- Alterar `speed` para mudar velocidade (aumentar = mais rápido)
- Mudar `frameDelay` para acelerar/desacelerar animação

**Modificar colisão:**
- Mudar `if (distance < 50)` para aumentar/diminuir zona de colisão

---

### 2. Character.java (O PERSONAGEM)

**O que faz:**
- Gerencia vida (HP)  
- Sistema de XP e leveling (níveis 1-10)
- Gerencia inventário
- Controla arma equipada
- Armazena dinheiro coletado

**Sistema de Leveling:**
```
Nível 1: 10 XP necessário
Nível 2: 15 XP necessário (+5)
Nível 3: 20 XP necessário (+5)
...
Nível 10: máximo (cap)

A cada level:
- Vida +2
- Espaço mochila +5 (nos níveis 5 e 10)
```

**Modificar leveling:**
- Mudar `xpNecessary = 10` para mudar XP inicial
- Em `calculateLevel()`, mudar `xpNecessary += 5` para ajustar curva de XP
- Mudar `maxLife += 2` para ganho de vida por level

---

### 3. Battle.java (SISTEMA DE BATALHA)

**O que faz:**
- Cria interface de combate
- Mostra personagem e inimigo
- Exibe barras de vida
- Processa ações (atacar, defender, fugir)
- Anima ataques

**Variáveis:**
- `playerHP = 100` → Vida do jogador em batalha
- `enemyHP = 80` → Vida do inimigo em batalha

**Modificar dano:**
- `enemyHP -= 10` → Dano que o jogador faz (aumentar número = mais dano)
- `playerHP -= 8` → Dano que o inimigo faz (aumentar número = mais dano)

**Modificar animações:**
- `Duration.millis(80)` → Velocidade de piscar (aumentar = mais lento)
- `Duration.millis(600)` → Tempo de espera entre ataques

---

### 4. Monsters.java (CLASSE BASE DE INIMIGOS)

**O que faz:**
- Define propriedades de TODOS os inimigos
- Método `attack()` para inimigo atacar

**Subclasses:**
- `Goblin` → Fraco (vida 8, dano 2)
- `GoblinExp` → Médio (vida 12, dano 3)  
- `GoblinBoss` → Forte (vida 25, dano 5)

**Adicionar novo inimigo:**
```java
class NovoTipo extends Monsters {
    public NovoTipo() {
        super("Nome", vida, dano, moedas, xp, velocidade, resistencia);
    }
}
```

---

### 5. Sword.java (SISTEMA DE ARMAS)

**O que faz:**
- Define como cada arma calcula dano
- Sistema de crítico baseado em rolagem de dado (1-20)
- Modificadores por raridade

**Armas Disponíveis:**

🔪 **ADAGA** → Ataca 2 vezes
- Se rolar 20: 2x dano DESSE ataque
- Total = até 2 ataques

⚔️ **KATANA** → Crítico devastador
- Comum: 2x dano em crítico
- Rara: 3x dano em crítico
- Lendária: 4x dano em crítico

🗡️ **ESPADA LONGA** → Múltiplos críticos
- Comum: crítico só com 20
- Rara: crítico com 18, 19, 20
- Lendária: crítico com 15-20

**Adicionar nova arma:**
```java
case "NoveArma" -> {
    int danoFinal = damage;
    if (dado == 20) {
        danoFinal *= 2;
    }
    danoTotal = danoFinal;
}
```

---

### 6. Inventory.java (SISTEMA DE MOCHILA)

**O que faz:**
- Armazena itens coletados
- Controla espaço

**Modificar:**
- `maxSpace = 20` → Tamanho inicial da mochila
- `increaseSpace(5)` → Aumenta espaço em 5

---

### 7. Item.java (CLASSE BASE DE ITENS)

**O que faz:**
- Define propriedades básicas de TODO item
- Subclasses: Sword, Armor, Potion

---

### 8. Armor.java (SISTEMA DE ARMADURA)

**O que faz:**
- Fornece resistência

**Como funciona:**
```
Dano Recebido = Dano Atacante - Resistência
Se resultado negativo = 0 (sem dano)
```

**Exemplo:**
- Inimigo faz 10 dano
- Você tem 8 de resistência
- Você recebe: 10 - 8 = 2 dano

---

### 9. Potion.java (POÇÕES DE CURA)

**O que faz:**
- Define quantidade de cura

**Exemplo:**
```java
Potion pequena = new Potion("Poção Pequena", 50, 1, 20);
// Cura 20 de vida, custa 50 moedas, ocupa 1 espaço
```

---

### 10. StartScreen.java (TELA INICIAL)

**O que faz:**
- Mostra tela inicial com título e botão
- Detecta resolução da tela automaticamente
- Adapta tamanho de elementos (responsivo)

**Modificar aparência:**
- Title: `"-fx-font-size: ` em `createStartScene()`
- Cores: `-fx-background-color`, `-fx-text-fill`

---

### 11. Goblin.java (INIMIGO TIPO GOBLIN)

**O que faz:**
- Define o Goblin comum

---

## COMO MODIFICAR O JOGO

### Aumentar Dificuldade:
1. Em **Battle.java**: Aumentar `enemyHP -= 10` para maior dano do inimigo
2. Em **Monsters.java**: Aumentar vida/dano nas subclasses
3. Em **Sword.java**: Diminuir bônus de crítico

### Adicionar Novo Inimigo:
1. Criar classe em **Monsters.java** herdando de Monsters
2. Definir vida, dano, XP, moedas
3. Em **App.java**: Mudar `new Goblin()` para novo inimigo

### Adicionar Nova Arma:
1. Em **Sword.java**: Adicionar `case` com nome da arma
2. Definir como calcula dano
3. Em **App.java**: Criarinstância da arma

### Mudar Leveling:
1. Em **Character.java**: `calculateLevel()`
2. Mudar `maxLife += 2` para ganho de vida
3. Mudar `xpNecessary += 5` para curva de XP

---

## DICAS PARA ENTENDER

1. **Sempre comece por App.java** - é o "ponto de entrada"
2. **Veja começar pelos comentários** - cada método tem explicação
3. **Procure "MODIFICAR" nos comentários** - estas seções explicam variaçõs
4. **Métodos comentados têm "Como funciona"** - seção que explica passo a passo

---

**Última atualização**: Março 2026  
**Desenvolvido para**: Quem não entende programação Java  
**Objetivo**: Permitir modificações sem conhecimento prévio
