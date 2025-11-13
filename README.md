# **SmartCoffee (Demo PPS)**
Demonstração de como implementar o **PlugPagServiceWrapper** em uma aplicação Android na **Moderninha Smart**.

## PlugPagServiceWrapper
Instruções de como adicionar o **PlugPagServiceWrapper** na sua aplicação podem ser encontradas [aqui](https://github.com/pagseguro/PlugPagServiceWrapper).

## Suporte
Caso precise de ajuda você pode encontrar mais informações [aqui](https://developer.pagbank.com.br/docs/integracao-smartpos) 

## Orientações gerais para o uso do PlugPagServiceWrapper
- Utilize o Wrapper como um Singleton em sua aplicação.
- Opte por adicionar Logs nos retornos do Wrapper isso pode ajudar a idenficar mais rapido problemas.
- Não faça chamadas de comandos blocantes de forma paralela.
  - Ex.: Pagamento, Estorno, Ativação, Reimpressão de comprovantes, Cálculo de parcelas, Pré-autorizadas, Verificação de capacidades, Subadquirente.
- Evite chamar a Ativação com terminal já ativado, prefira verificar com o método `isAuthenticated`.
- Antes de iniciar uma operação blocante verifique se o serviço não esta ocupado com o método `isServiceBusy`.
