//***************************************************************************
//
//	Description.: осциллограф-самописец
//
//	Version.....: 0.2
//
//  Target(s) mcu...: mega8
//
//  Compiler....: gcc-4.3.3 (WinAVR 2010.01.10)
//
//	Comment(s)..: только при приеме по USART символов '1', '2', '3', '4'  мк посылает
//				значение 1-го  2-го  3-го 4-го канала ацп преобразования соответственно. 
//				<!-- Т.к. результат каждого ацп преобразования
//				представляет собой 10-и битный результат, а данные передаются по 8-и 
//				битному каналу, то для для того чтобы передать значение измеренноое ацп
//				нужно результат преобразования делить на 4. В итоге в программе на ПК
//				будкт строится графики по 8-и битным значениям. -->
//
//  Data........: 09.05.14
//	
//***************************************************************************

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <util/atomic.h>

#define		LED1	0b00001000
// #define LED2 0b00000100
#define		OFF_ALL_LEDS	0x00
#define		LED_PAUSE		_delay_ms(500)

#define		HIGH_7	(_BV(7))
#define		HIGH_6	(_BV(6))
#define		HIGH_5	(_BV(5))
#define		HIGH_4	(_BV(4))
#define		HIGH_3	(_BV(3))
#define		HIGH_2	(_BV(2))
#define		HIGH_1	(_BV(1))
#define		HIGH_0	(_BV(0))
#define		LOW_7	(~(_BV(7)))
#define		LOW_6	(~(_BV(6)))
#define		LOW_5	(~(_BV(5)))
#define		LOW_4	(~(_BV(4)))
#define		LOW_3	(~(_BV(3)))
#define		LOW_2	(~(_BV(2)))
#define		LOW_1	(~(_BV(1)))

// #define 	ADC1_REFINT		(_BV(7))|(_BV(6))|(~(_BV(5)))|(~(_BV(4)))|(~(_BV(3)))|(~(_BV(2)))|(~(_BV(1)))|(_BV(0))
// #define 	ADC1_REFINT		HIGH_7 | HIGH_6 | LOW_5 | LOW_4 | LOW_3 | LOW_2 | LOW_1 | HIGH_0
#define 	ADC1_REFINT		HIGH_7 | HIGH_6 | HIGH_0
#define 	ADC2_REFINT		HIGH_7 | HIGH_6 | HIGH_1
#define 	ADC3_REFINT		HIGH_7 | HIGH_6 | HIGH_1 | HIGH_0
#define 	ADC4_REFINT		HIGH_7 | HIGH_6 | HIGH_2	

// #define 	ADC1_REFINT		0b11000001	//внутр ИОН=2,56В; active ADC1
// #define 	ADC2_REFINT		0b11000010	//внутр ИОН=2,56В; active ADC2
// #define 	ADC3_REFINT		0b11000011	//внутр ИОН=2,56В; active ADC3





void init_io(void);
void init_adc(void);
void init_usart(void);
void blik_led1(void);
// void blik_led2(void);
void sendCharToUSART(unsigned char sym);
unsigned char getCharOfUSART(void);

//однобайтный буфер
volatile unsigned char usartRxBuf = 0;
volatile unsigned int val1, val2, val3, val4;
volatile unsigned char lowByte;
volatile unsigned int adcResult;


int main(void){
	unsigned char sym;
	cli();
	init_io();
	init_adc();
	init_usart();
	sei();
	
	ADCSRA |= (1<<ADSC); // запускаем первое АЦП преобразование
	
	while(1){
		sym = getCharOfUSART();
		if(sym == '1'){
			sendCharToUSART((unsigned char)(val1/4));
		}else if(sym == '2'){
			sendCharToUSART((unsigned char)(val2/4));
		}else if(sym == '3'){
			sendCharToUSART((unsigned char)(val3/4));
		}else if(sym == '4'){
			sendCharToUSART((unsigned char)(val4/4));
		}
	}
	
	// while(1){
		// blik_led1();
	// }
}
//===========================
//прием символа по usart`у в буфер
ISR(USART_RXC_vect){ 
   usartRxBuf = UDR;  
} 

ISR(ADC_vect){
	// 1. считываем младший и старший байты результата АЦ-преобразования и образуем из них 10-битовый результат
	lowByte = ADCL;
	adcResult = (ADCH<<8)|lowByte;

	// 2. В зависимости от номера канала ADC сохраняем результат в ячейке памяти и настраиваем номер канала для следующего преобразования
	switch (ADMUX) {
		case ADC1_REFINT:
			val1 = adcResult;
			ADMUX = ADC2_REFINT;
			break;
		case ADC2_REFINT:
			val2 = adcResult;
			ADMUX = ADC3_REFINT;
			break;
		case ADC3_REFINT:
			val3 = adcResult;
			ADMUX = ADC4_REFINT;
			break;
		case ADC4_REFINT:
			val4 = adcResult;
			ADMUX = ADC1_REFINT;
			break;
		default:
		//...
		break;
	}
	// 3. запускаем новое АЦ-преобразование
	ADCSRA |= (1<<ADSC);
}
//===========================

void init_io(void){
	DDRB = (_BV(3));
}

void init_adc(void){
	ADCSRA |= (1<<ADPS2)|(1<<ADPS1)|(1<<ADPS0); // предделитель на 128
	ADCSRA |= (1<<ADIE);                        // разрешаем прерывание от ацп
	ADCSRA |= (1<<ADEN);                        // разрешаем работу АЦП

	ADMUX |= (1<<REFS0)|(1<<REFS1);             // работа от внутр. ИОН 2,56 В
	ADMUX|=(0<<MUX3)|(0<<MUX2)|(0<<MUX1)|(1<<MUX0);
	//ADMUX|=0b11000001;
}

void init_usart(void){
	// Table 82 rus datasheet mega8
	// UBRRL = 103; //103 -скорость обмена 1200 бод для 2 Mhz cpu
	// UBRRL = 25; //25 -скорость обмена 4800 бод для 2 Mhz cpu
	// UBRRL = 12; //12 -скорость обмена 9600 бод для 2 Mhz cpu
	
	
	// Значение которое пишется в UBRR зависит от тактовой частоты MCU
	// Вычисляется по формуле из табл. 52 datasheet на atmega8
	// Например для fcpu = 8 MHz, для асинхронного нормального режима
	// и требуемой скорости 9600 бод : UBBR = (8 MHz)/16*9600 - 1 = 51
	
	//UBRR=766 @ 1200 бод при 14,7456 MHz (U2X = 0)
	// UBRRH = 0x02;
	// UBRRL = 0xFE; 
	
	//UBRR=95 @ 9600 бод при 14,7456 MHz (U2X = 0)
	// UBRRH = 0;
	// UBRRL = 95; 
	
	//UBRR=47 @ 19200 бод при 14,7456 MHz (U2X = 0)
	// --------- самый оптимальный вариант (16 выб/с для 4 канала)
	UBRRH = 0;
	UBRRL = 47; 
	
	//UBRR=23 @ 38400 бод при 14,7456 MHz (U2X = 0)
	// UBRRH = 0;
	// UBRRL = 23; 
	
	//UBRR=15 @ 57600 бод при 14,7456 MHz (U2X = 0)
	// UBRRH = 0;
	// UBRRL = 15; 
	
	// UCSRA=(1<<U2X);
	
	UCSRB=(1<<RXCIE)|(1<<RXEN)|(1<<TXEN); //разр. прерыв при приеме, разр приема, разр передачи.
	UCSRC=(1<<URSEL)|(1<<UCSZ1)|(1<<UCSZ0);  //размер слова 8 разрядов
}
//=====================================

//отправка символа по usart`у
void sendCharToUSART(unsigned char sym){
	while(!(UCSRA & (1<<UDRE)));
	UDR = sym;  
}

//чтение буфера
unsigned char getCharOfUSART(void){
	unsigned char tmp;
	ATOMIC_BLOCK(ATOMIC_FORCEON){
		tmp = usartRxBuf;
		usartRxBuf = 0;
	}
	return tmp;  
}

/*
	Тестовая ф-ия для проверки правильности работы макросов
*/
void blik_led1(void){
	//сюда нужно подставить проверяемый макрос
	// if(0b11000001 == 0b11000011){
	// if( ((_BV(7)) | (_BV(6)) | (_BV(0))) == 0b11000011){
	if( (ADC1_REFINT) == 0b11000001){
		PORTB = LED1;
		LED_PAUSE;
		PORTB = OFF_ALL_LEDS;
		LED_PAUSE;
	}
}

// void blik_led2(void){
	// PORTB = LED2;
	// _delay_ms(LED_PAUSE);
	// PORTB = OFF_ALL_LEDS;
	// //_delay_ms(300);
// }
