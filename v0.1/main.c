//***************************************************************************
//
//	Description.: осциллограф-самописец
//
//	Version.....: 0.1
//
//  Target(s) mcu...: mega8
//
//  Compiler....: gcc-4.3.3 (WinAVR 2010.01.10)
//
//	Comment(s)..: только при приеме по USART символов '1' и '2'  мк посылает
//				значение 1-го канала и 2-го канала ацп преобразования соответственно. 
//				<!-- Т.к. результат каждого ацп преобразования
//				представляет собой 10-и битный результат, а данные передаются по 8-и 
//				битному каналу, то для для того чтобы передать значение измеренноое ацп
//				нужно результат преобразования делить на 4. В итоге в программе на ПК
//				будкт строится графики по 8-и битным значениям. -->
//				-- нужно отформатировать код
//
//  Data........: 06.05.14
//	
//***************************************************************************

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <util/atomic.h>

#define LED1 0b00000010
#define LED2 0b00000100
#define OFF_ALL_LEDS 0b00000000
#define LED_PAUSE 100

void init_io(void);
void init_adc(void);
void init_usart(void);
void blik_led1(void);
void blik_led2(void);
void sendCharToUSART(unsigned char sym);
unsigned char getCharOfUSART(void);

//однобайтный буфер
volatile unsigned char usartRxBuf = 0;

// unsigned char asciiTable[] = {'!', '"', '#', '$', '%', '&', '(', ')', '*', '+'};

volatile unsigned int val1, val2;
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
			// blik_led1();
			// blik_led2();
			sendCharToUSART((unsigned char)(val1/4));
			// _delay_ms(100);
		}else if(sym == '2'){
			sendCharToUSART((unsigned char)(val2/4));
			// _delay_ms(100);
		}
	}
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
		case 0xC1:
			val1 = adcResult;
			//ADMUX = 0xC2;
			//ADMUX|=(1<<REFS0)|(1<<REFS1) | (0<<MUX3)|(0<<MUX2)|(1<<MUX1)|(0<<MUX0);
			ADMUX=0b11000010;
			break;
		case 0xC2:
			val2 = adcResult;
			//ADMUX = 0xC1;
			//ADMUX|=(1<<REFS0)|(1<<REFS1) | (0<<MUX3)|(0<<MUX2)|(0<<MUX1)|(1<<MUX0);
			ADMUX=0b11000001;
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
	//pb1 pb2
	// DDRB = 0b00000110;
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
	UBRRH = 0;
	// Table 82 rus datasheet mega8
	// UBRRL = 103; //103 -скорость обмена 1200 бод для 2 Mhz cpu
	// UBRRL = 25; //25 -скорость обмена 4800 бод для 2 Mhz cpu
	// UBRRL = 12; //12 -скорость обмена 9600 бод для 2 Mhz cpu
	
	// Значение которое пишется в UBRR зависит от тактовой частоты MCU
	// Вычисляется по формуле из табл. 52 datasheet на atmega8
	// Например для fcpu = 8 MHz, для асинхронного нормального режима
	// и требуемой скорости 9600 бод : UBBR = (8 MHz)/16*9600 - 1 = 51
	
	//UBRR=766 @ 1200 бод при 14,7456 MHz
	UBRRH = 0x02;
	UBRRL = 0xFE; 
	
	//UBRR=95 @ 9600 бод при 14,7456 MHz
	// UBRRL = 95; 
	
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


/* void blik_led1(void){
	PORTB = LED1;
	_delay_ms(LED_PAUSE);
	PORTB = OFF_ALL_LEDS;
	//_delay_ms(300);

}

void blik_led2(void){
	PORTB = LED2;
	_delay_ms(LED_PAUSE);
	PORTB = OFF_ALL_LEDS;
	//_delay_ms(300);
} */
