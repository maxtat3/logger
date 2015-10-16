//***************************************************************************
//
//	Description.: осциллограф-самописец
//
//	Version.....: 0.3
//
//  Target(s) mcu...: mega8
//
//  Compiler....: gcc-4.3.3 (WinAVR 2010.01.10)
//
//	Comment(s)..: добавлена возможность установки количества
//					выборок (семплов) в секунду.
//					добавлены две тестовые ф-ии на светодиодах.
//
//  Data........: 10.05.14
//	
//***************************************************************************

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <util/atomic.h>

#define nop()  __asm__ __volatile__("nop")

#define		LED1	0b00001000
#define		LED2	0b00010000
#define		OFF_ALL_LEDS	0x00
#define		LED_PAUSE		_delay_ms(100)

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

#define 	ADC1_REFINT		HIGH_7 | HIGH_6 | HIGH_0
#define 	ADC2_REFINT		HIGH_7 | HIGH_6 | HIGH_1
#define 	ADC3_REFINT		HIGH_7 | HIGH_6 | HIGH_1 | HIGH_0
#define 	ADC4_REFINT		HIGH_7 | HIGH_6 | HIGH_2	
// #define 	ADC1_REFINT		0b11000001	//внутр ИОН=2,56В; active ADC1
// #define 	ADC2_REFINT		0b11000010	//внутр ИОН=2,56В; active ADC2
// #define 	ADC3_REFINT		0b11000011	//внутр ИОН=2,56В; active ADC3

#define		DELAY_15_SPS_4CH		nop()
#define		DELAY_10_SPS_4CH		_delay_ms(25)
#define		DELAY_4_SPS_4CH			_delay_ms(57)


void init_io(void);
void init_adc(void);
void init_usart(void);
void blik_led1(void);
void blik_led2(void);
void sendCharToUSART(unsigned char sym);
unsigned char getCharOfUSART(void);


volatile unsigned char usartRxBuf = 0;	//однобайтный буфер
volatile unsigned int val1, val2, val3, val4;
volatile unsigned char lowByte;
volatile unsigned int adcResult;
volatile unsigned char requestToChangeSamplePerSecond = 0;
volatile unsigned char userSamplesPerSecond = 0;


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

		if(requestToChangeSamplePerSecond == 1){
			if(sym == '0' || sym == '1' || sym == '2'){
				userSamplesPerSecond = sym;
				requestToChangeSamplePerSecond = 0;
			}
		}
		if(sym == 's'){
			requestToChangeSamplePerSecond = 1;
		}
		
		if(sym == '1'){
			sendCharToUSART((unsigned char)(val1/4));
		}else if(sym == '2'){
			sendCharToUSART((unsigned char)(val2/4));
		}else if(sym == '3'){
			sendCharToUSART((unsigned char)(val3/4));
		}else if(sym == '4'){
			sendCharToUSART((unsigned char)(val4/4));
		}
		if(userSamplesPerSecond == '0'){
			DELAY_15_SPS_4CH;
		}else if(userSamplesPerSecond == '1'){
			DELAY_10_SPS_4CH;
		}else if(userSamplesPerSecond == '2'){
			DELAY_4_SPS_4CH;
		}
		
	}
	// while(1){
		// blik_led1();
	// }
}


// прием символа по usart`у в буфер
ISR(USART_RXC_vect){ 
   usartRxBuf = UDR;  
} 

// обработка прерывания от ацп
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


// настройка портов в/в
void init_io(void){
	DDRB |= (_BV(3));
	DDRB |= (_BV(4));
}

// настройка АЦП
void init_adc(void){
	ADCSRA |= (1<<ADPS2)|(1<<ADPS1)|(1<<ADPS0); // предделитель на 128
	ADCSRA |= (1<<ADIE);                        // разрешаем прерывание от ацп
	ADCSRA |= (1<<ADEN);                        // разрешаем работу АЦП

	ADMUX |= (1<<REFS0)|(1<<REFS1);             // работа от внутр. ИОН 2,56 В
	ADMUX|=(0<<MUX3)|(0<<MUX2)|(0<<MUX1)|(1<<MUX0);
	//ADMUX|=0b11000001;
}

// настройка USART
void init_usart(void){
	// UBRR=47 @ 19200 бод при 14,7456 MHz (U2X = 0)
	// самый оптимальный вариант (16 выб/с для 4 канала)
	// UBRRH = 0;
	// UBRRL = 47; 
	
	//UBRR=95 @ 9600 бод при 14,7456 MHz (U2X = 0)
	// примерно 15 выб/с для 4 канала
	UBRRH = 0;
	UBRRL = 95; 
	
	// UCSRA=(1<<U2X);
	UCSRB=(1<<RXCIE)|(1<<RXEN)|(1<<TXEN); //разр. прерыв при приеме, разр приема, разр передачи.
	UCSRC=(1<<URSEL)|(1<<UCSZ1)|(1<<UCSZ0);  //размер слова 8 разрядов
}

// отправка символа по usart`у
void sendCharToUSART(unsigned char sym){
	while(!(UCSRA & (1<<UDRE)));
	UDR = sym;  
}

// чтение буфера
unsigned char getCharOfUSART(void){
	unsigned char tmp;
	ATOMIC_BLOCK(ATOMIC_FORCEON){
		tmp = usartRxBuf;
		usartRxBuf = 0;
	}
	return tmp;  
}

// тестовая ф-ия 1
void blik_led1(void){
	PORTB = LED1;
	LED_PAUSE;
	PORTB = OFF_ALL_LEDS;
	LED_PAUSE;
}

// тестовая ф-ия 2
void blik_led2(void){
	PORTB = LED2;
	LED_PAUSE;
	PORTB = OFF_ALL_LEDS;
	LED_PAUSE;
}
