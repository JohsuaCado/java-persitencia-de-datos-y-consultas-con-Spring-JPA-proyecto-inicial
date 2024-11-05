package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.*;
import com.aluracursos.screenmatch.repository.SerieRepository;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=4a968531";
    private ConvierteDatos conversor = new ConvierteDatos();
    private List<DatosSerie>datosSeries=new ArrayList<>();
    private SerieRepository repository;
    private List<Serie>series;
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repository) {
        this.repository=repository;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    1 - Buscar series 
                    2 - Buscar episodios
                    3 - Mostrar series buscadas
                    4 - Buscar Series por Titulo
                    5 - Top 5 Mejores Series
                    6 - Buscar series por categoria
                    7 - Filtrar series
                    8 - Buscar Episodio por Titulo
                    9 - Top 5 Episodios por Serie 
                                  
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    mostrarSeriesBuscados();
                    break;
                case 4:
                    buscarSeriesPorTitulo();
                    break;
                case 5:
                    findTop5ByOrderByEvaluacionDesc();
                    break;
                case 6:
                    buscarSeriesPorCategoria();
                    break;
                case 7:
                    filtrarSeriesPorTemporadaYEvaluacion();
                    break;
                case 8:
                    buscarEpisodioPorTitulo();
                    break;
                case 9:
                    buscarTop5Episodios();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }


    private DatosSerie getDatosSerie() {
        System.out.println("Escribe el nombre de la serie que deseas buscar");
        var nombreSerie = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreSerie.replace(" ", "+") + API_KEY);
        System.out.println(json);
        DatosSerie datos = conversor.obtenerDatos(json, DatosSerie.class);
        return datos;
    }
    private void buscarEpisodioPorSerie() {
        mostrarSeriesBuscados();
        System.out.println("Escribe el nombre de la serie de la cual quieres ver los episodios:");
        var nombreSerie= teclado.nextLine();

        Optional<Serie>serie=series.stream()
                .filter(s-> s.getTitulo().toLowerCase().contains(nombreSerie.toLowerCase()))
                .findFirst();
        if (serie.isPresent()){
            var serieEncontrada=serie.get();
            List<DatosTemporadas> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoApi.obtenerDatos(URL_BASE + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DatosTemporadas datosTemporada = conversor.obtenerDatos(json, DatosTemporadas.class);
                temporadas.add(datosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio>episodios=temporadas.stream()
                    .flatMap(d-> d.episodios().stream()
                            .map(e-> new Episodio(d.numero(),e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repository.save(serieEncontrada);
        }

    }
    private void buscarSerieWeb() {
        DatosSerie datos = getDatosSerie();
        Serie serie=new Serie(datos);
        repository.save(serie);
        //datosSeries.add(datos);
        System.out.println(datos);
    }

    private void mostrarSeriesBuscados() {
        series=repository.findAll();
        //series=datosSeries.stream()
                //.map(d-> new Serie(d))
                //.collect(Collectors.toList());

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriesPorTitulo(){
        System.out.println("Escribe el nombre de la serie de la cual quieres ver:");
        var nombreSerie= teclado.nextLine();
        serieBuscada= repository.findByTituloContainsIgnoreCase(nombreSerie);
        if(serieBuscada.isPresent()){
            System.out.println("La serie buscada es: "+ serieBuscada.get());
        }else {
            System.out.println("Serie no encontrada");
        }

    }

    private void findTop5ByOrderByEvaluacionDesc(){
        List<Serie>topSeries=repository.findTop5ByOrderByEvaluacionDesc();
        topSeries.forEach(s ->
                System.out.println("Serie: "+ s.getTitulo()+" Evaluacion: "+ s.getEvaluacion()));
    }

    private void buscarSeriesPorCategoria(){
        System.out.println("Escribe el genero/categoria de la serie:");
        var genero=teclado.nextLine();
        var categoria= Categoria.fromEspanol(genero);
        List<Serie>seriesPorCategoria=repository.findByGenero(categoria);
        System.out.println("Las series de la categoria "+ genero);
        seriesPorCategoria.forEach(System.out::println);
    }
    public void filtrarSeriesPorTemporadaYEvaluacion(){
        System.out.println("¿Filtrar séries con cuántas temporadas? ");
        var totalTemporadas = teclado.nextInt();
        teclado.nextLine();
        System.out.println("¿Con evaluación apartir de cuál valor? ");
        var evaluacion = teclado.nextDouble();
        teclado.nextLine();
        List<Serie> filtroSeries = repository.seriesPorTemporadasYEvaluacion(totalTemporadas,evaluacion);
        System.out.println("*** Series filtradas ***");
        filtroSeries.forEach(s ->
                System.out.println(s.getTitulo() + "  - evaluacion: " + s.getEvaluacion()));
    }

    private void buscarEpisodioPorTitulo(){
        System.out.println("Escribe el nombre del titulo:");
        var nombreEpisodio = teclado.nextLine();
        List<Episodio>episodiosEncontrados = repository.episodiosPorNombre(nombreEpisodio);
        episodiosEncontrados.forEach(e->
                System.out.printf("Series: %s Temporada %s Episodio %s Evaluacion %s\n",
                        e.getSerie(),e.getTemporada(),e.getNumeroEpisodio(), e.getEvaluacion()));
    }

    private void buscarTop5Episodios(){
        buscarEpisodioPorTitulo();
        if (serieBuscada.isPresent()){
            Serie serie=serieBuscada.get();
            List<Episodio>topEpisodios = repository.top5Episodios(serie);
            topEpisodios.forEach(e->
                    System.out.printf("Series: %s Temporada %s Episodio %s Evaluacion %s\n",
                            e.getSerie(),e.getTemporada(),e.getNumeroEpisodio(), e.getEvaluacion()));
        }
    }
}

